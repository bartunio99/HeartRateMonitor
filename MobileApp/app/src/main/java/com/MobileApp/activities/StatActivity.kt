package com.mobileapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobileapp.R
import com.mobileapp.database.Session
import com.mobileapp.database.pulseDatabase
import com.mobileapp.adapters.dbAdapter
import com.mobileapp.databinding.StatLayoutBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StatActivity: ComponentActivity(){
    private lateinit var adapter: dbAdapter
    private var selectedSession: Session?= null
    private lateinit var binding: StatLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = StatLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val undoButton: Button = findViewById(R.id.undoBtn)
        val statButton: Button = findViewById(R.id.statBtn)
        val deleteButton: Button = findViewById(R.id.deleteBtn)

        val db = pulseDatabase.getInstance(this)
        val sessionDao = db.sessionDao()
        var chosenSession: Int = 0
        var sessionList: List<Session> = emptyList()
        var sessions: MutableList<Session> = mutableListOf()

        // Pobranie danych z DAO w Coroutine
        lifecycleScope.launch {
            sessionList = sessionDao.getAllSessions()
            withContext(Dispatchers.Main) {
                // Aktualizacja UI na głównym wątku
                sessionList.forEach { session ->
                    Log.d("Database", "Session ID: ${session.session_id}, Date: ${session.date}")
                }
            }

            sessions = sessionList.toMutableList()

            adapter = dbAdapter(sessions){ session ->
                selectedSession = session
                chosenSession = session.session_id
                Toast.makeText(this@StatActivity,"Wybrano sesje", Toast.LENGTH_SHORT).show()
            }
            binding.recyclerView.apply{
                layoutManager = LinearLayoutManager(this@StatActivity)
                adapter = this@StatActivity.adapter
            }

        }

        statButton.setOnClickListener{
            if(chosenSession!=null && chosenSession>0) {
                val statIntent = Intent(this, StatViewActivity::class.java).apply {
                    putExtra("session_id", chosenSession)
                }
                startActivity(statIntent)
            }
        }

        deleteButton.setOnClickListener {
            val selectedSessionID = selectedSession?.session_id
            lifecycleScope.launch {
                if(selectedSessionID!=null){
                    sessionDao.deleteRecord(selectedSessionID)
                    adapter.notifyDataSetChanged()
                }
            }
        }

        undoButton.setOnClickListener{
            finish()
        }

    }


}