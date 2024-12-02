package com.mobileapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.mobileapp.R

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
        btButton.setOnClickListener{
            val btIntent = Intent(this, BtClient::class.java)
            startActivity(btIntent)
        }
        statButton.setOnClickListener{
            val statIntent = Intent(this, StatActivity::class.java)
            startActivity(statIntent)
        }

        exitButton.setOnClickListener {
            finish()
        }



    }
}
