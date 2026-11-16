package com.example.alleswiegen.utils

import android.content.Context
import android.net.wifi.WifiManager
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Locale

object NetworkUtils {

    fun isWifiConnected(context: Context): Boolean {
        return getWifiIpAddress(context) != null
    }

    fun getWifiIpAddress(context: Context): String? {
        // 1. Check WifiManager (when connected as Wi-Fi client)
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            @Suppress("DEPRECATION")
            val ipInt = wifiManager?.connectionInfo?.ipAddress ?: 0
            if (ipInt != 0) {
                return String.format(
                    Locale.US, "%d.%d.%d.%d",
                    ipInt and 0xff,
                    ipInt shr 8 and 0xff,
                    ipInt shr 16 and 0xff,
                    ipInt shr 24 and 0xff
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Check NetworkInterfaces (works for both Wi-Fi Client and Hotspot / Tethering mode)
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            val candidateAddresses = mutableListOf<String>()

            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val name = networkInterface.name.lowercase()

                // Look for WLAN, Access Point, SoftAP, Tethering or Ethernet interfaces
                if (name.contains("wlan") || name.contains("ap") || name.contains("softap") ||
                    name.contains("rndis") || name.contains("swlan") || name.contains("p2p") ||
                    name.contains("eth")) {

                    val addresses = networkInterface.inetAddresses
                    while (addresses.hasMoreElements()) {
                        val addr = addresses.nextElement()
                        if (!addr.isLoopbackAddress && addr is Inet4Address) {
                            val host = addr.hostAddress ?: continue
                            // Exclude APIPA / Link-local (169.254.x.x)
                            if (!host.startsWith("169.254.")) {
                                candidateAddresses.add(host)
                            }
                        }
                    }
                }
            }

            // Return first matching private IPv4 address (192.168.x.x, 172.x.x.x, 10.x.x.x)
            val privateIp = candidateAddresses.firstOrNull { ip ->
                ip.startsWith("192.168.") || ip.startsWith("10.") ||
                        (ip.startsWith("172.") && ip.split(".").getOrNull(1)?.toIntOrNull() in 16..31)
            }
            if (privateIp != null) return privateIp

            // Fallback to any IPv4 address found
            if (candidateAddresses.isNotEmpty()) return candidateAddresses.first()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }
}
