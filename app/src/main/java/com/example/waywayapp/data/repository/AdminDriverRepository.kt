package com.example.waywayapp.data.repository

import com.example.waywayapp.data.model.AdminDriver
import com.example.waywayapp.core.firebase.FirestoreProvider
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AdminDriverRepository {
    private val firestore = FirestoreProvider.db

    fun observeDrivers(): Flow<List<AdminDriver>> = callbackFlow {
        val registration = firestore.collection("drivers")
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val drivers = snapshot
                    ?.documents
                    ?.mapNotNull { document ->
                        document.toObject(AdminDriver::class.java)
                            ?.copy(id = document.id)
                    }
                    .orEmpty()

                trySend(drivers)
            }

        awaitClose {
            registration.remove()
        }
    }

    suspend fun saveDriver(
        driver: AdminDriver
    ) {
        val now = System.currentTimeMillis()
        val document =
            if (driver.id.isBlank()) {
                firestore.collection("drivers").document()
            } else {
                firestore.collection("drivers").document(driver.id)
            }

        val savedDriver = driver.copy(
            id = document.id,
            updatedAt = now,
            createdAt = if (driver.createdAt == 0L) now else driver.createdAt
        )

        document.set(savedDriver, SetOptions.merge()).await()
    }
}
