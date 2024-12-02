package com.mobileapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface pulseDataDao{
    @Insert
    suspend fun insertPulseData(pulseData: PulseData)

    @Query("SELECT * FROM PULSEDATA WHERE SESSION_ID = :session_id ORDER BY TIME ASC")  //wyciaga tylko ta sesje ktora podam w parametrze
    suspend fun getPulseDataForSession(session_id: Int): List<PulseData>
}