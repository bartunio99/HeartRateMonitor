package com.mobileapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Observer

class pulseClient: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pulse_layout)

        val handler = Handler(Looper.getMainLooper())

        val pulseDisplay: TextView = findViewById(R.id.pulseData)
        val exitButton: Button = findViewById(R.id.undoBtn)
        val connector = intent.getSerializableExtra("connector") as? btConnect

        exitButton.setOnClickListener{
            finish()
            connector!!.disconnect()
        }

        pulseSingleton.addListener { newPulseValue ->
            handler.post {
                pulseDisplay.text = newPulseValue.toString()
            }

        }


    }



    override fun onDestroy() {

        super.onDestroy()
    }
}