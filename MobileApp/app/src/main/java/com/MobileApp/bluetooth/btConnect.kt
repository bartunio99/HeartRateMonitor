package com.mobileapp.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.mobileapp.objects.pulseSingleton
import databaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.util.*

class btConnect(private val context: Context) {

    private var bluetoothGatt: BluetoothGatt? = null
    private var pulseData: MutableList<Int> = mutableListOf<Int>()
    @RequiresApi(Build.VERSION_CODES.O)
    var dateStart: LocalDate = LocalDate.now()
    @RequiresApi(Build.VERSION_CODES.O)
    var timeStart: Instant = Instant.now()

    var threadRun: Boolean = true





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
            @SuppressLint("NewApi")
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                Log.d("btConnect", "Stan połączenia zmieniony: status=$status, newState=$newState")

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d("btConnect", "Połączono z urządzeniem: ${device.name}")
                    onSuccess()
                    gatt?.discoverServices()  // Odkrywanie usług urządzenia
                    dateStart = LocalDate.now()
                    timeStart = Instant.now()
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
            while (threadRun) {
                if (characteristic != null) {
                    // Próbujemy odczytać charakterystykę tętna
                    gatt.readCharacteristic(characteristic)
                    Log.d("btConnect", "Próba odczytu charakterystyki tętna.")
                } else {
                    Log.e("btConnect", "Nie znaleziono charakterystyki tętna.")
                    break
                }
                Thread.sleep(50)  // Przerwa 5 sekund przed kolejnym odczytem
            }
        }.start()
    }


    // Funkcja do rozłączenia urządzenia
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    fun disconnect() {
        bluetoothGatt?.close()
        bluetoothGatt = null
        threadRun = false
        startSessionAndPulseData(timeStart,dateStart,pulseData)

    }

    //konwersja danych na inty
    fun byteArrayToInt(bytes: ByteArray): Int {
        require(bytes.size == 2) { "ByteArray must be 2 bytes long" }
        return ((bytes[0].toInt() and 0xFF) shl 8) or
                (bytes[1].toInt() and 0xFF)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startSessionAndPulseData(startTime: Instant, startDate: LocalDate, pulseList: MutableList<Int>) {
        val sessionManager = databaseManager(context)

        // Wywołanie metody w coroutine
        CoroutineScope(Dispatchers.IO).launch {
            sessionManager.addSessionAndPulseData(startTime, startDate, pulseList)  // Wywołanie metody z SessionManager
            Log.d("Database","Dane sesji i pulsu zostały zapisane.")
        }
    }

}


