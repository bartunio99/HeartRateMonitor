package com.MobileApp

import android.Manifest.permission.*
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.MobileApp.databinding.BtLayoutBinding

@SuppressLint("MissingPermission")
class btClient :ComponentActivity() {

    val PERMISSION_CODE = 101

    private lateinit var binding: BtLayoutBinding
    private lateinit var adapter: recyclerAdapter
    private lateinit var bleScanner: btScan
    private val devices = mutableListOf<BluetoothDevice>()
    private lateinit var bluetoothAdapter: BluetoothAdapter



    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = BtLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = recyclerAdapter(devices)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@btClient)
            adapter = this@btClient.adapter
        }

        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        bleScanner = btScan(bluetoothAdapter)

        //define buttons
        val undoButton: Button = findViewById(R.id.undoBtn)
        val scanButton: Button = findViewById(R.id.scanBtn)


        scanButton.setOnClickListener {
            if (bluetoothAdapter.isEnabled) {
                checkPermission(BLUETOOTH_SCAN, PERMISSION_CODE)
                checkPermission(BLUETOOTH_CONNECT, PERMISSION_CODE)
                startBleScanning()
            }else{
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivity(enableBtIntent)
            }

        }


        undoButton.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bleScanner.stopScanning()
    }


    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this@btClient, permission) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(this@btClient, arrayOf(permission), requestCode)
        } else {
            Toast.makeText(this@btClient, "Permission already granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@btClient, "Bt Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@btClient, "Bt Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startBleScanning() {
        bleScanner.startScanning { device ->
            runOnUiThread {
                adapter.updateDevices(listOf(device))
            }
        }
    }

    private fun checkBluetoothEnabled(): Boolean {
        return if (!bluetoothAdapter.isEnabled) {
            // Prompt the user to enable Bluetooth
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(enableBtIntent)
            false
        } else {
            true
        }
    }




}

