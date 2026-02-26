package com.workspace.exoplanethunter.exoplanet.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ExoplanetEntity::class], version = 1, exportSchema = false)
abstract class ExoplanetDatabase : RoomDatabase() {

    abstract fun exoplanetDao(): ExoplanetDao

    companion object {
        @Volatile
        private var INSTANCE: ExoplanetDatabase? = null

        fun getInstance(context: Context): ExoplanetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExoplanetDatabase::class.java,
                    "exoplanet_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
