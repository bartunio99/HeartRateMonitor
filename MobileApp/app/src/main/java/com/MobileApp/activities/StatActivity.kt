package com.mobileapp.activities

import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import com.mobileapp.R

class statActivity: ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stat_layout)

        val undoButton: Button = findViewById(R.id.undoBtn)

        undoButton.setOnClickListener{
            finish()
        }

    }
}