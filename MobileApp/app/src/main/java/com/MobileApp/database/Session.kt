package com.mobileapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalTime
import java.util.Date

@Entity
data class Session(
    @PrimaryKey(autoGenerate = true) val session_id: Int = 0,
    val date: Date,
    val start_time: LocalTime,
    val end_time: LocalTime
)