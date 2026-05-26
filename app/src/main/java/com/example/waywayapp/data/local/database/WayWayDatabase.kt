package com.example.waywayapp.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.waywayapp.data.local.dao.CartDao
import com.example.waywayapp.data.local.entity.CartEntity

@Database(
    entities = [
        CartEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class WayWayDatabase : RoomDatabase() {

    abstract fun cartDao(): CartDao
}
