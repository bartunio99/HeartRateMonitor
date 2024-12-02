package com.mobileapp.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult

class btScan(private val bluetoothAdapter: BluetoothAdapter) {

    private val scanner: BluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    private val foundDevices = mutableListOf<BluetoothDevice>()
    private var scanCallback: ScanCallback? = null

    @SuppressLint("MissingPermission")
    fun startScanning(onDeviceFound: (BluetoothDevice) -> Unit) {
        if (scanCallback == null) {
            scanCallback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    val device = result.device
                    if (!foundDevices.contains(device)) {
                        foundDevices.add(device)
                        onDeviceFound(device) // Informujemy o znalezionym urządzeniu
                    }
                }

                override fun onScanFailed(errorCode: Int) {
                    super.onScanFailed(errorCode)
                }
            }
            scanner.startScan(scanCallback)
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        scanCallback?.let {
            scanner.stopScan(it)
            scanCallback = null
        }
    }




}
