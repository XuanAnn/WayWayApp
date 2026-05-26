package com.example.waywayapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.waywayapp.data.local.entity.CartEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    @Query("SELECT * FROM cart")
    fun getCartItems(): Flow<List<CartEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(
        item: CartEntity
    )

    @Query("""
        UPDATE cart
        SET quantity = :quantity
        WHERE foodId = :foodId
    """)
    suspend fun updateQuantity(
        foodId: Int,
        quantity: Int
    )

    @Query("""
        DELETE FROM cart
        WHERE foodId = :foodId
    """)
    suspend fun deleteCartItem(
        foodId: Int
    )

    @Query("DELETE FROM cart")
    suspend fun clearCart()
}