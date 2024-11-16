package com.MobileApp

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu)

        //initialize screen
        val welcomeText: TextView = findViewById(R.id.text)
        val btButton: Button = findViewById(R.id.btBtn)
        val statButton : Button = findViewById(R.id.statBtn)
        val exitButton: Button = findViewById(R.id.exitBtn)

        //button listeners
//        btButton.setOnClickListener{
//            val btIntent = Intent(this, btConnect::class.java)
//            startActivity(btIntent)
//        }
        exitButton.setOnClickListener {
            finish()
        }







    }
}
