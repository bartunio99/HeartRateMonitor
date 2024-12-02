package com.mobileapp.adapters

import android.bluetooth.BluetoothDevice
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.mobileapp.database.Session
import com.mobileapp.databinding.ItemLayoutBinding
import java.time.temporal.ChronoUnit

class dbAdapter(
    private val sessions: MutableList<Session>,
    private val onSessionClick: (Session) -> Unit  // Funkcja do obsługi kliknięcia
) : RecyclerView.Adapter<dbAdapter.SessionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val binding = ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SessionViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val session = sessions[position]
        holder.binding.textViewTitle.text = "Sesja nr " + session.session_id.toString()  //id sesji
        holder.binding.textViewSubtitle.text = session.start_time.truncatedTo(ChronoUnit.SECONDS).toString() + " " + session.end_time.truncatedTo(ChronoUnit.SECONDS).toString()               //data

        // Obsługa kliknięcia na urządzenie
        holder.binding.root.setOnClickListener {
            onSessionClick(session)  // Wywołanie funkcji, która obsługuje kliknięcie
        }
    }

    fun updateDevices(newDevices: List<Session>) {
        // Dodaj nowe urządzenia, unikając duplikatów
        newDevices.forEach { session ->
            if (!sessions.contains(session)) {
                sessions.add(session)
            }
        }
        notifyDataSetChanged()  // Powiadom adapter o zmianach
    }


    override fun getItemCount(): Int {
        return sessions.size
    }

    class SessionViewHolder(val binding: ItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)
}
