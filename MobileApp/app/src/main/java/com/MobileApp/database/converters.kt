package com.mobileapp.database

import android.annotation.SuppressLint
import androidx.room.TypeConverter
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date

class converters {
    // Date -> Long
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    // Long -> Date
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }

    // LocalTime -> String
    @SuppressLint("NewApi")
    @TypeConverter
    fun fromLocalTime(localTime: LocalTime?): String? {
        return localTime?.format(DateTimeFormatter.ISO_LOCAL_TIME)
    }

    // String -> LocalTime
    @SuppressLint("NewApi")
    @TypeConverter
    fun toLocalTime(timeString: String?): LocalTime? {
        return timeString?.let { LocalTime.parse(it, DateTimeFormatter.ISO_LOCAL_TIME) }
    }
}