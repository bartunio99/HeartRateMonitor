package com.MobileApp

import android.Manifest.permission.*
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import android.Manifest
import android.annotation.SuppressLint
import android.util.Log

@SuppressLint("MissingPermission")
class btClient :ComponentActivity(){
    val ALL_BLE_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        )
    }
    else {
        arrayOf(
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }


    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var deviceList: MutableList<BluetoothDevice>
    private lateinit var adapter: ArrayAdapter<String>

    val scanner = btScan(
        context = this, // "this" odnosi się do kontekstu Activity
        onDeviceFound = { result ->
            // Kod, który zostanie wywołany, gdy urządzenie BLE zostanie znalezione

            Log.d("Bluetooth", "Found device: ${result.device.name}")
        },
        onScanFailed = { errorCode ->
            // Kod, który zostanie wywołany, gdy skanowanie zakończy się niepowodzeniem
            Log.e("Bluetooth", "Scan failed with error code: $errorCode")
        }
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bt_layout)

        //define buttons
        val undoButton: Button = findViewById(R.id.undoBtn)
        val scanButton: Button = findViewById(R.id.scanBtn)



        scanButton.setOnClickListener{
            scanner.requestBluetoothPermission(requestPermissionLauncher)
            scanner.startScan()
        }

        undoButton.setOnClickListener{
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scanner.stopScan()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        scanner.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "hi!", Toast.LENGTH_SHORT).show()
            // Jeśli uprawnienie zostało przyznane, rozpoczynamy skanowanie
            scanner.startScan()

        } else {
            // Jeśli uprawnienie zostało odrzucone, pokazujemy komunikat
            Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show()
        }
    }

}