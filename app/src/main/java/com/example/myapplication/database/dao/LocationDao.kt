package com.example.myapplication.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.myapplication.database.entity.Location

@Dao
interface LocationDao {
    @Query("select * from location")
    fun getLocations(): List<Location>

    @Insert
    fun insertLocation(location: Location)
}