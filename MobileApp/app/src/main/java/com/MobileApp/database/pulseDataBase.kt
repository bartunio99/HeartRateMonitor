package com.mobileapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Session::class, PulseData::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(converters::class)
abstract class pulseDatabase: RoomDatabase(){
    abstract fun sessionDao(): sessionDao
    abstract fun pulseDataDao(): pulseDataDao

    companion object {

        /**
         * As we need only one instance of db in our app will use to store
         * This is to avoid memory leaks in android when there exist multiple instances of db
         */
        @Volatile
        private var INSTANCE: pulseDatabase? = null

        fun getInstance(context: Context): pulseDatabase {

            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        pulseDatabase::class.java,
                        "pulseDatabase"
                    ).build()

                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}





