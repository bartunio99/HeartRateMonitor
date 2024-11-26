package com.MobileApp

import android.Manifest
import android.Manifest.permission.BLUETOOTH_SCAN
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class btScan(
    private val context: Context,
    private val onDeviceFound: (device: ScanResult) -> Unit,
    private val onScanFailed: (errorCode: Int) -> Unit,
) {
    private val REQUEST_PERMISSION_CODE = 1

    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            onDeviceFound(result)
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            super.onBatchScanResults(results)
            results.forEach { onDeviceFound(it) }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            onScanFailed(errorCode)
        }
    }

    @RequiresPermission(BLUETOOTH_SCAN)
    fun startScan() {
//        if (!checkPermissions()) {
//            Toast.makeText(context, "Permissions denied!", Toast.LENGTH_SHORT).show()
//        }

        if (bluetoothAdapter?.isEnabled == true) {
            if (bluetoothLeScanner != null) {
                bluetoothLeScanner.startScan(scanCallback)
            }

            Toast.makeText(context, "Scanning started...", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Bluetooth is disabled.", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresPermission(BLUETOOTH_SCAN)
    fun stopScan() {
        bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        Toast.makeText(context, "Scanning stopped.", Toast.LENGTH_SHORT).show()
    }

    private fun checkPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestBluetoothPermission(
        permissionLauncher: ActivityResultLauncher<String>
    ) {
        // Sprawdzenie, czy uprawnienie zostało już przyznane
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED) {
            // Jeśli uprawnienie jest już przyznane
            //Toast.makeText(context, "Permission already granted.", Toast.LENGTH_SHORT).show()
        } else {
            // Jeśli uprawnienie nie zostało przyznane, uruchamiamy żądanie
            permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
        }
    }




    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(context, "Permissions granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Permissions denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
