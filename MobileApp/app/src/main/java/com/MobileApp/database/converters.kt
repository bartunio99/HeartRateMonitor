package com.mobileapp.database

import android.annotation.SuppressLint
import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Date

class converters {

    // Date -> Long (dla Instant)
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    // Long -> Date (dla Instant)
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }

    // LocalDate -> String
    @SuppressLint("NewApi")
    @TypeConverter
    fun fromLocalDate(localDate: LocalDate?): String? {
        return localDate?.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    // String -> LocalDate
    @SuppressLint("NewApi")
    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }
    }

    // Instant -> String
    @SuppressLint("NewApi")
    @TypeConverter
    fun fromInstant(instant: Instant?): String? {
        return instant?.toString() // Zapisuje w formacie ISO 8601 (np. 2024-12-01T12:34:56Z)
    }

    // String -> Instant
    @SuppressLint("NewApi")
    @TypeConverter
    fun toInstant(instantString: String?): Instant? {
        return instantString?.let { Instant.parse(it) }
    }
}
