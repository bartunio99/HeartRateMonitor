package com.mobileapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface sessionDao{
    @Insert
    suspend fun insertSession(session: Session): Long

    @Update
    suspend fun updateSession(session: Session)

    @Query("DELETE FROM SESSION WHERE SESSION_ID = :session_id ")
    suspend fun deleteRecord(session_id: Int)

    @Query("SELECT * FROM SESSION")
    suspend fun getAllSessions(): List<Session>
}