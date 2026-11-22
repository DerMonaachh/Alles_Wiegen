package com.example.alleswiegen

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.*
import kotlin.math.round

data class WeightResult(
    val weight: Int,
    val isStable: Boolean
)

data class DiscoveredBluetoothDevice(
    val name: String,
    val address: String,
    val rssi: Int,
    val isScaleCandidate: Boolean
)

class BluetoothService(private val context: Context, private val adapter: BluetoothAdapter?) {

    private var bluetoothGatt: BluetoothGatt? = null
    private var scanCallback: ScanCallback? = null
    private val handler = Handler(Looper.getMainLooper())

    // Deine identifizierten UUIDs
    private var SERVICE_UUID = "fff0".toBleUuid()
    private var CHARACTERISTIC_UUID = "fff1".toBleUuid()
    //private val SERVICE_UUID = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb")
    //private val CHARACTERISTIC_UUID = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb")
    private val CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    fun String.toBleUuid(): UUID {
        return if (this.length <= 4) {
            // Macht aus "fff0" -> "0000fff0-0000-1000-8000-00805f9b34fb"
            UUID.fromString("0000$this-0000-1000-8000-00805f9b34fb")
        } else {
            UUID.fromString(this)
        }
    }
    @SuppressLint("MissingPermission")
    fun connect(
        address: String,
        onWeightReceived: (Int, Boolean, String) -> Unit,
        onStatusChanged: (Boolean) -> Unit,
        serviceUuid: String,
        characteristicUuid: String
    ) {
        try {
            stop()
            SERVICE_UUID = serviceUuid.toBleUuid()
            CHARACTERISTIC_UUID = characteristicUuid.toBleUuid()
            val device = adapter?.getRemoteDevice(address)

            bluetoothGatt = device?.connectGatt(context, false, object : BluetoothGattCallback() {
                override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        onStatusChanged(true)
                        try {
                            gatt.discoverServices()
                        } catch (e: SecurityException) {
                            Log.e("BluetoothService", "SecurityException discovering services", e)
                            onStatusChanged(false)
                        }
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        onStatusChanged(false)
                    }
                }

                override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                    try {
                        val characteristic = gatt.getService(SERVICE_UUID)?.getCharacteristic(CHARACTERISTIC_UUID)
                        if (characteristic != null) {
                            gatt.setCharacteristicNotification(characteristic, true)
                            val descriptor = characteristic.getDescriptor(CONFIG_DESCRIPTOR_UUID)
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            gatt.writeDescriptor(descriptor)
                        }
                    } catch (e: SecurityException) {
                        Log.e("BluetoothService", "SecurityException on services discovered", e)
                        onStatusChanged(false)
                    }
                }

                override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
                    if (characteristic.uuid == CHARACTERISTIC_UUID) {
                        val data = characteristic.value
                        val hexString = data.joinToString(" ") { "%02X".format(it) }

                        // 3. Hier rufen wir die neue extractWeight-Methode auf
                        val result = extractWeight(data)

                        // 4. Werte an das UI / die Activity übergeben
                        onWeightReceived(result.weight, result.isStable, hexString)
                    }
                }
            })
        } catch (e: SecurityException) {
            Log.e("BluetoothService", "SecurityException when connecting GATT", e)
            onStatusChanged(false)
        } catch (e: Exception) {
            Log.e("BluetoothService", "Error connecting GATT", e)
            onStatusChanged(false)
        }
    }

    // Deine spezifische Umrechnung: Byte 10-11, Big Endian, Gramm * 10
    private fun extractWeight(data: ByteArray): WeightResult {
        return try {
            if (data.size >= 11) {
                val statusByte = data[8].toInt() and 0xFF

                // Bits auslesen
                val isTenthsOfGram = (statusByte and 0x10) != 0
                val isStable = (statusByte and 0x08) != 0 // Das neue Stabil-Bit!

                // Gewicht berechnen
                val highByte = data[9].toInt() and 0xFF
                val lowByte = data[10].toInt() and 0xFF
                val rawValue = (highByte shl 8) or lowByte

                val divisor = if (isTenthsOfGram) 10.0 else 1.0
                val calculatedWeight = round(rawValue.toDouble() / divisor).toInt()

                // Hinweis: `round()` entfernt dir die Nachkommastelle bei den Zehntelgramm!
                // Wenn du 292.5g sehen willst, lass das round() weg oder nutze es nur für den Gramm-Modus.
                WeightResult(calculatedWeight, isStable)
            } else {
                WeightResult(0, false)
            }
        } catch (e: Exception) {
            WeightResult(0, false)
        }
    }

    @SuppressLint("MissingPermission")
    fun stop() {
        try {
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
        } catch (e: Exception) {
            Log.e("BluetoothService", "Error stopping BluetoothGatt", e)
        } finally {
            bluetoothGatt = null
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan(
        onDeviceDiscovered: (DiscoveredBluetoothDevice) -> Unit,
        onScanFinished: () -> Unit,
        scanDurationMs: Long = 10000L
    ) {
        stopScan()
        val scanner = adapter?.bluetoothLeScanner
        if (scanner == null) {
            onScanFinished()
            return
        }

        val discoveredAddresses = mutableSetOf<String>()

        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = result.device ?: return
                val address = device.address ?: return

                if (!discoveredAddresses.contains(address)) {
                    discoveredAddresses.add(address)
                    val rawName = device.name ?: result.scanRecord?.deviceName
                    val deviceName = if (!rawName.isNullOrBlank()) rawName else "Unbekanntes Gerät"

                    // Check if it's a scale candidate
                    val serviceUuids = result.scanRecord?.serviceUuids?.map { it.uuid } ?: emptyList()
                    val fff0Uuid = "fff0".toBleUuid()
                    val devInfoUuid = "180a".toBleUuid()

                    val isScaleUuid = serviceUuids.contains(fff0Uuid) || serviceUuids.contains(devInfoUuid)
                    val isScaleName = deviceName.contains("Scale", ignoreCase = true) ||
                            deviceName.contains("Qing", ignoreCase = true) ||
                            deviceName.contains("Ching", ignoreCase = true) ||
                            deviceName.contains("Senssun", ignoreCase = true) ||
                            deviceName.contains("Nutri", ignoreCase = true) ||
                            deviceName.contains("iScale", ignoreCase = true) ||
                            deviceName.startsWith("QN-", ignoreCase = true) ||
                            deviceName.startsWith("IC-", ignoreCase = true)

                    val isScaleCandidate = isScaleUuid || isScaleName

                    onDeviceDiscovered(
                        DiscoveredBluetoothDevice(
                            name = deviceName,
                            address = address,
                            rssi = result.rssi,
                            isScaleCandidate = isScaleCandidate
                        )
                    )
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e("BluetoothService", "BLE Scan failed with code $errorCode")
                stopScan()
                onScanFinished()
            }
        }

        try {
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()
            scanner.startScan(null, settings, scanCallback)

            handler.postDelayed({
                stopScan()
                onScanFinished()
            }, scanDurationMs)
        } catch (e: SecurityException) {
            Log.e("BluetoothService", "SecurityException starting BLE scan", e)
            stopScan()
            onScanFinished()
        } catch (e: Exception) {
            Log.e("BluetoothService", "Error starting BLE scan", e)
            stopScan()
            onScanFinished()
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        try {
            if (scanCallback != null) {
                adapter?.bluetoothLeScanner?.stopScan(scanCallback)
                scanCallback = null
            }
        } catch (e: Exception) {
            Log.e("BluetoothService", "Error stopping BLE scan", e)
        }
    }
}