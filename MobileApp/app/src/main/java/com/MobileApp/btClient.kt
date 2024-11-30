package com.mobileapp

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
import com.mobileapp.databinding.BtLayoutBinding
import java.util.UUID

@SuppressLint("MissingPermission")
class btClient :ComponentActivity() {

    private val uuid: UUID = UUID.fromString("0000180D-0000-1000-8000-00805F9B34FB") // Standardowy UUID SPP
    val PERMISSION_CODE = 101

    private lateinit var binding: BtLayoutBinding
    private lateinit var adapter: recyclerAdapter
    private lateinit var bleScanner: btScan
    private val devices = mutableListOf<BluetoothDevice>()
    private lateinit var btConnector: btConnect
    private var selectedDevice: BluetoothDevice? = null



    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = BtLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        btConnector = btConnect(this)

        adapter = recyclerAdapter(devices) { device ->
            selectedDevice = device  // Przechowuj wybrane urządzenie
            Toast.makeText(this, "Wybrano urządzenie: ${device.name ?: "Nieznane"}, ${device.address?: "Nieznane"}", Toast.LENGTH_SHORT).show()
        }
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
        val connectButton: Button = findViewById(R.id.connectBtn)


        scanButton.setOnClickListener {
            if (bluetoothAdapter.isEnabled) {
                checkPermission(BLUETOOTH_SCAN, PERMISSION_CODE)
                checkPermission(BLUETOOTH_CONNECT, PERMISSION_CODE)
                startBleScanning()
            }else{
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                enableBtIntent.putExtra("connector", btConnector)
                startActivity(enableBtIntent)

            }

        }

        connectButton.setOnClickListener{
            selectedDevice?.let { device ->
                connectToDevice(device)
            } ?: Toast.makeText(this, "Wybierz urządzenie przed połączeniem.", Toast.LENGTH_SHORT).show()

            val pulseIntent = Intent(this, pulseClient::class.java)
            startActivity(pulseIntent)
        }



        undoButton.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bleScanner.stopScanning()
        btConnector.disconnect()
    }


    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this@btClient, permission) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(this@btClient, arrayOf(permission), requestCode)
        } else {
            //Toast.makeText(this@btClient, "Permission already granted", Toast.LENGTH_SHORT).show()
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

    private fun connectToDevice(device: BluetoothDevice) {
        btConnector.connectToDevice(device,
            onSuccess = {
                //Toast.makeText(this, "Połączono z urządzeniem ${device.name}", Toast.LENGTH_SHORT).show()
            },
            onError = { error ->
                //Toast.makeText(this, "Błąd: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }





}

