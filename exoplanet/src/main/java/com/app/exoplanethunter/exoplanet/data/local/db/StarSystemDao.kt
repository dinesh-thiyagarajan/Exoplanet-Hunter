package com.app.exoplanethunter.exoplanet.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface StarSystemDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(system: StarSystemEntity): Long
}
