package com.mobileapp.activities

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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobileapp.R
import com.mobileapp.bluetooth.btConnect
import com.mobileapp.bluetooth.btScan
import com.mobileapp.databinding.BtLayoutBinding
import com.mobileapp.adapters.btAdapter
import java.util.UUID

@SuppressLint("MissingPermission")
class BtClient :ComponentActivity() {

    private val uuid: UUID = UUID.fromString("0000180D-0000-1000-8000-00805F9B34FB") // Standardowy UUID SPP
    val PERMISSION_CODE = 101

    private lateinit var binding: BtLayoutBinding
    private lateinit var adapter: btAdapter
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



        adapter = btAdapter(devices) { device ->
            selectedDevice = device  // Przechowuj wybrane urządzenie
            Toast.makeText(this, "Wybrano urządzenie: ${device.name ?: "Nieznane"}, ${device.address?: "Nieznane"}", Toast.LENGTH_SHORT).show()
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@BtClient)
            adapter = this@BtClient.adapter
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
                startActivity(enableBtIntent)
            }

        }

        connectButton.setOnClickListener{
            selectedDevice?.let { device ->

                connectToDevice(device)
            } ?: Toast.makeText(this, "Wybierz urządzenie przed połączeniem.", Toast.LENGTH_SHORT).show()

            val pulseIntent = Intent(this, PulseClient::class.java)
            startPulseClientForResult.launch(pulseIntent)
        }



        undoButton.setOnClickListener {
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        super.onDestroy()
        bleScanner.stopScanning()
    }


    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this@BtClient, permission) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(this@BtClient, arrayOf(permission), requestCode)
        } else {
            Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@BtClient, "Bt Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@BtClient, "Bt Permission Denied", Toast.LENGTH_SHORT).show()
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

    @RequiresApi(Build.VERSION_CODES.O)
    private val startPulseClientForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == 0) {
            // Wynik, gdy PulseClient zakończy się poprawnie
            btConnector.disconnect()

            Toast.makeText(this, "PulseClient zakończone", Toast.LENGTH_SHORT).show()
        } else {
            // Jeśli PulseClient zakończy się z innym wynikiem
            Toast.makeText(this, "PulseClient zakończone z błędem ${result.resultCode}", Toast.LENGTH_SHORT).show()
        }
    }







}

