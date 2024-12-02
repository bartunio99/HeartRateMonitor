package com.mobileapp.activities

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import com.mobileapp.R
import com.mobileapp.objects.pulseSingleton

class PulseClient: ComponentActivity() {


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pulse_layout)

        val handler = Handler(Looper.getMainLooper())

        val pulseDisplay: TextView = findViewById(R.id.pulseData)
        val exitButton: Button = findViewById(R.id.undoBtn)

        exitButton.setOnClickListener{
            finish()
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