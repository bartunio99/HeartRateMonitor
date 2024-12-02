package com.mobileapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

@Entity
data class Session(
    @PrimaryKey(autoGenerate = true) val session_id: Int = 0,
    val date: LocalDate,
    val start_time: Instant,
    val end_time: Instant
)