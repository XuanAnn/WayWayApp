package com.example.waywayapp.core.database

import android.content.Context
import androidx.room.Room
import com.example.waywayapp.data.local.database.WayWayDatabase

object DatabaseProvider {

    private var INSTANCE: WayWayDatabase? = null

    fun getDatabase(
        context: Context
    ): WayWayDatabase {

        return INSTANCE ?: synchronized(this) {

            val instance =
                Room.databaseBuilder(
                    context.applicationContext,
                    WayWayDatabase::class.java,
                    "wayway_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

            INSTANCE = instance

            instance
        }
    }
}
