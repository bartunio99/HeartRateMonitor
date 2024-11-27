package com.MobileApp

import android.Manifest.permission.BLUETOOTH_CONNECT
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.annotation.RequiresPermission
import com.MobileApp.databinding.ItemLayoutBinding

class recyclerAdapter(private val devices: MutableList<BluetoothDevice>) : RecyclerView.Adapter<recyclerAdapter.DeviceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviceViewHolder(binding)
    }

    @RequiresPermission(BLUETOOTH_CONNECT)
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]
        holder.binding.textViewTitle.text = device.name ?: "Unknown Device"
        holder.binding.textViewSubtitle.text = device.address
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    fun updateDevices(newDevices: List<BluetoothDevice>) {
        // Dodaj nowe urządzenia, unikając duplikatów
        newDevices.forEach { device ->
            if (!devices.contains(device)) {
                devices.add(device)
            }
        }
        notifyDataSetChanged()  // Powiadom adapter o zmianach
    }


    class DeviceViewHolder(val binding: ItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)
}