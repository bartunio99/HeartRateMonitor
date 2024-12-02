package com.mobileapp.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "PulseData",
    foreignKeys = [ForeignKey(
        entity = Session::class,
        parentColumns = ["session_id"],
        childColumns = ["session_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [androidx.room.Index(value = ["session_id"])]
)
data class PulseData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val session_id: Int,       // ID sesji, do której należy pomiar
    val time: Long,           // Czas trwania sesji (np. w milisekundach lub sekundach)
    val pulse: Int           // Pomiar tętna
)