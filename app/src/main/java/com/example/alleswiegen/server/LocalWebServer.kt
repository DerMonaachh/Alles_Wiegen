package com.example.alleswiegen.server

import android.content.Context
import android.util.Base64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

class LocalWebServer {

    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private val activeClients = ConcurrentHashMap.newKeySet<Socket>()
    private var lastBroadcastJson: String? = null

    @Volatile
    var isRunning: Boolean = false
        private set

    fun start(context: Context, port: Int = 8080, onClientConnected: (() -> Unit)? = null) {
        if (isRunning) return

        val htmlContent = try {
            context.assets.open("index.html").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
            "<!DOCTYPE html><html><body><h1>index.html nicht gefunden</h1></body></html>"
        }

        isRunning = true
        serverJob = scope.launch {
            try {
                serverSocket = ServerSocket(port)
                println("LocalWebServer gestartet auf Port $port")

                while (isRunning) {
                    val socket = serverSocket?.accept() ?: break
                    scope.launch {
                        handleClient(socket, htmlContent, onClientConnected)
                    }
                }
            } catch (e: Exception) {
                if (isRunning) {
                    e.printStackTrace()
                }
            } finally {
                stop()
            }
        }
    }

    private fun handleClient(socket: Socket, htmlContent: String, onClientConnected: (() -> Unit)?) {
        try {
            val inputStream = socket.getInputStream()
            val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))

            val requestLine = reader.readLine() ?: run {
                socket.close()
                return
            }

            val headers = mutableMapOf<String, String>()
            var line: String?
            while (reader.readLine().also { line = it } != null && line!!.isNotBlank()) {
                val parts = line!!.split(":", limit = 2)
                if (parts.size == 2) {
                    headers[parts[0].trim().lowercase()] = parts[1].trim()
                }
            }

            if (requestLine.startsWith("GET /ws") && headers["upgrade"]?.lowercase() == "websocket") {
                // Handle WebSocket Handshake
                val webSocketKey = headers["sec-websocket-key"]
                if (webSocketKey != null) {
                    performWebSocketHandshake(socket.getOutputStream(), webSocketKey)
                    activeClients.add(socket)
                    println("WebSocket Client verbunden: ${socket.remoteSocketAddress}")

                    // Send latest state if available
                    lastBroadcastJson?.let { sendFrame(socket.getOutputStream(), it) }
                    onClientConnected?.invoke()

                    // Listen for WebSocket frames (read until close or error)
                    listenToWebSocket(socket)
                } else {
                    socket.close()
                }
            } else {
                // Serve HTTP GET index.html or 404
                val outputStream = socket.getOutputStream()
                if (requestLine.startsWith("GET / ") || requestLine.startsWith("GET /index.html")) {
                    val bytes = htmlContent.toByteArray(Charsets.UTF_8)
                    val response = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: text/html; charset=utf-8\r\n" +
                            "Content-Length: ${bytes.size}\r\n" +
                            "Connection: close\r\n\r\n"
                    outputStream.write(response.toByteArray(Charsets.UTF_8))
                    outputStream.write(bytes)
                    outputStream.flush()
                } else {
                    val notFound = "HTTP/1.1 404 Not Found\r\nContent-Length: 0\r\nConnection: close\r\n\r\n"
                    outputStream.write(notFound.toByteArray(Charsets.UTF_8))
                    outputStream.flush()
                }
                socket.close()
            }
        } catch (e: Exception) {
            activeClients.remove(socket)
            try { socket.close() } catch (_: Exception) {}
        }
    }

    private fun performWebSocketHandshake(outputStream: OutputStream, clientKey: String) {
        val magicKey = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
        val acceptKey = Base64.encodeToString(
            MessageDigest.getInstance("SHA-1").digest((clientKey + magicKey).toByteArray(Charsets.UTF_8)),
            Base64.NO_WRAP
        )
        val response = "HTTP/1.1 101 Switching Protocols\r\n" +
                "Upgrade: websocket\r\n" +
                "Connection: Upgrade\r\n" +
                "Sec-WebSocket-Accept: $acceptKey\r\n\r\n"
        outputStream.write(response.toByteArray(Charsets.UTF_8))
        outputStream.flush()
    }

    private fun listenToWebSocket(socket: Socket) {
        try {
            val input = socket.getInputStream()
            while (isRunning && !socket.isClosed) {
                val b1 = input.read()
                if (b1 == -1) break
                val opcode = b1 and 0x0F

                val b2 = input.read()
                if (b2 == -1) break
                val isMasked = (b2 and 0x80) != 0
                var payloadLen = (b2 and 0x7F).toLong()

                if (payloadLen == 126L) {
                    val b3 = input.read()
                    val b4 = input.read()
                    if (b3 == -1 || b4 == -1) break
                    payloadLen = ((b3 and 0xFF) shl 8 or (b4 and 0xFF)).toLong()
                } else if (payloadLen == 127L) {
                    var len: Long = 0
                    for (i in 0 until 8) {
                        val b = input.read()
                        if (b == -1) break
                        len = (len shl 8) or (b and 0xFF).toLong()
                    }
                    payloadLen = len
                }

                val maskingKey = ByteArray(4)
                if (isMasked) {
                    if (input.read(maskingKey) != 4) break
                }

                // Read payload bytes
                val payload = ByteArray(payloadLen.toInt())
                var bytesRead = 0
                while (bytesRead < payloadLen) {
                    val r = input.read(payload, bytesRead, payloadLen.toInt() - bytesRead)
                    if (r == -1) break
                    bytesRead += r
                }

                if (opcode == 0x8) { // Close frame
                    break
                }
            }
        } catch (_: Exception) {
        } finally {
            activeClients.remove(socket)
            try { socket.close() } catch (_: Exception) {}
        }
    }

    fun broadcastJson(json: String) {
        lastBroadcastJson = json
        val frame = encodeWebSocketTextFrame(json)
        val iterator = activeClients.iterator()
        while (iterator.hasNext()) {
            val client = iterator.next()
            try {
                if (client.isClosed || !client.isConnected) {
                    iterator.remove()
                    continue
                }
                sendFrame(client.getOutputStream(), frame)
            } catch (e: Exception) {
                iterator.remove()
                try { client.close() } catch (_: Exception) {}
            }
        }
    }

    private fun sendFrame(outputStream: OutputStream, frame: ByteArray) {
        synchronized(outputStream) {
            outputStream.write(frame)
            outputStream.flush()
        }
    }

    private fun sendFrame(outputStream: OutputStream, message: String) {
        val frame = encodeWebSocketTextFrame(message)
        sendFrame(outputStream, frame)
    }

    private fun encodeWebSocketTextFrame(text: String): ByteArray {
        val payload = text.toByteArray(Charsets.UTF_8)
        val length = payload.size
        val out = ByteArrayOutputStream()
        out.write(0x81) // FIN=1, Opcode=1 (Text)
        when {
            length <= 125 -> {
                out.write(length)
            }
            length <= 65535 -> {
                out.write(126)
                out.write((length shr 8) and 0xFF)
                out.write(length and 0xFF)
            }
            else -> {
                out.write(127)
                for (i in 7 downTo 0) {
                    out.write(((length.toLong() shr (i * 8)) and 0xFF).toInt())
                }
            }
        }
        out.write(payload)
        return out.toByteArray()
    }

    fun stop() {
        isRunning = false
        for (client in activeClients) {
            try { client.close() } catch (_: Exception) {}
        }
        activeClients.clear()
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        serverSocket = null
        serverJob?.cancel()
        serverJob = null
    }
}
