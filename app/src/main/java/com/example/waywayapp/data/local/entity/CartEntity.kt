package com.example.waywayapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart")
data class CartEntity(

    @PrimaryKey
    val foodId: Int,

    val name: String,

    val description: String,

    val store: String,

    val imageRes: Int,

    val price: Double,

    val distance: String,

    val rating: Double,

    val badge: String,

    val quantity: Int
)
