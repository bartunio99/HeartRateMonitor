package com.MobileApp

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import java.util.*

class btConnect(private val context: Context) {

    private var bluetoothGatt: BluetoothGatt? = null

    // Funkcja do połączenia z urządzeniem
    @SuppressLint("MissingPermission")
    fun connectToDevice(
        device: BluetoothDevice,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        Log.d("btConnect", "Próba połączenia z urządzeniem: ${device.name}")

        bluetoothGatt = device.connectGatt(context, false, object : BluetoothGattCallback() {

            // Obsługuje zmianę stanu połączenia
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                Log.d("btConnect", "Stan połączenia zmieniony: status=$status, newState=$newState")

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d("btConnect", "Połączono z urządzeniem: ${device.name}")
                    onSuccess()
                    gatt?.discoverServices()  // Odkrywanie usług urządzenia
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d("btConnect", "Rozłączono z urządzeniem: ${device.name}")
                    onError("Rozłączono z urządzeniem.")
                } else {
                    Log.d("btConnect", "Inny stan połączenia: $newState")
                }
            }

            // Obsługuje odkrywanie usług
            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                super.onServicesDiscovered(gatt, status)
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d("btConnect", "Usługi odkryte.")
                    readHeartRateCharacteristic(gatt)  // Odczyt charakterystyki tętna
                } else {
                    Log.e("btConnect", "Błąd podczas odkrywania usług, status=$status")
                    onError("Błąd podczas odkrywania usług")
                }
            }

            // Obsługuje odczyt charakterystyki
            override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
                super.onCharacteristicRead(gatt, characteristic, status)
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    characteristic?.let {
                        Log.d("btConnect", "Odczytano charakterystykę: ${it.uuid}, wartość: ${it.value?.joinToString(", ")}")
                        // Tutaj możesz przetworzyć odczytane dane, np. zaktualizować UI
                    }
                } else {
                    Log.e("btConnect", "Błąd odczytu charakterystyki, status=$status")
                }
            }
        })
    }

    // Funkcja do odczytu charakterystyki tętna (np. z urządzenia HRM)
    @SuppressLint("MissingPermission")
    private fun readHeartRateCharacteristic(gatt: BluetoothGatt?) {
        val heartRateServiceUuid = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb")  // UUID dla usługi tętna
        val heartRateMeasurementUuid = UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb")  // UUID dla charakterystyki tętna

        val service = gatt?.getService(heartRateServiceUuid)
        val characteristic = service?.getCharacteristic(heartRateMeasurementUuid)

        if (characteristic != null) {
            // Próbujemy odczytać charakterystykę tętna
            gatt.readCharacteristic(characteristic)
            Log.d("btConnect", "Próba odczytu charakterystyki tętna.")
        } else {
            Log.e("btConnect", "Nie znaleziono charakterystyki tętna.")
        }
    }

    // Funkcja do rozłączenia urządzenia
    @SuppressLint("MissingPermission")
    fun disconnect() {
        bluetoothGatt?.close()
        bluetoothGatt = null
        Log.d("btConnect", "Połączenie rozłączone.")
    }
}
