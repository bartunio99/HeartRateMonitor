package com.mobileapp

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import com.mobileapp.database.pulseDatabase
import java.io.Serializable
import java.time.Instant
import java.time.LocalDate
import java.util.*

class btConnect(private val context: Context): Serializable {

    private var bluetoothGatt: BluetoothGatt? = null
    private var pulseData: MutableList<Int> = mutableListOf<Int>()
    val date: LocalDate?= null
    val time: Instant?= null

    val db = pulseDatabase.getInstance(context)
    val sessionDao = db.sessionDao()
    val pulseDataDao = db.pulseDataDao()


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

                        val pulse = byteArrayToInt(characteristic.value)
                        pulseSingleton.updatePulseValue(pulse)
                        pulseData.add(pulse)

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
        val heartRateServiceUuid =
            UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb")  // UUID dla usługi tętna
        val heartRateMeasurementUuid =
            UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb")  // UUID dla charakterystyki tętna

        val service = gatt?.getService(heartRateServiceUuid)
        val characteristic = service?.getCharacteristic(heartRateMeasurementUuid)

        Thread {
            while (true) {
                if (characteristic != null) {
                    // Próbujemy odczytać charakterystykę tętna
                    gatt.readCharacteristic(characteristic)
                    Log.d("btConnect", "Próba odczytu charakterystyki tętna.")
                } else {
                    Log.e("btConnect", "Nie znaleziono charakterystyki tętna.")
                    break
                }
                Thread.sleep(5000)  // Przerwa 5 sekund przed kolejnym odczytem
            }
        }.start()
    }


    // Funkcja do rozłączenia urządzenia
    @SuppressLint("MissingPermission")
    fun disconnect() {
        bluetoothGatt?.close()
        bluetoothGatt = null
        Log.d("btConnect", "Połączenie rozłączone.")
    }

    //konwersja danych na inty
    fun byteArrayToInt(bytes: ByteArray): Int {
        require(bytes.size == 2) { "ByteArray must be 2 bytes long" }
        return ((bytes[0].toInt() and 0xFF) shl 8) or
                (bytes[1].toInt() and 0xFF)
    }
}


