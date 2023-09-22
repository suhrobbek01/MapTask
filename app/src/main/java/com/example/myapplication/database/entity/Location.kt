package com.example.myapplication.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Location(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var longit: String? = null,
    var code: Int = 0,
    var lat: String? = null
)