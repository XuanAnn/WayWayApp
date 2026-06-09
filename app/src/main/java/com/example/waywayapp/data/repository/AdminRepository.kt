package com.example.waywayapp.data.repository

import com.example.waywayapp.core.firebase.FirestoreProvider
import com.example.waywayapp.data.model.AdminDriver
import com.example.waywayapp.data.model.AdminUser
import com.example.waywayapp.data.remote.dto.firestore.toAdminDriverDto
import com.example.waywayapp.data.remote.dto.firestore.toAdminUserDto
import com.example.waywayapp.data.remote.dto.firestore.toDto
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// Repository cho màn admin quản lý người dùng và tài xế trên Firestore.
class AdminRepository {
    // Firestore chứa collection users và drivers.
    private val firestore = FirestoreProvider.db

    // Lắng nghe realtime danh sách users
    fun observeUsers(): Flow<List<AdminUser>> = callbackFlow {
        val registration = firestore.collection("users")
            .limit(200)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val users = snapshot?.documents
                    ?.mapNotNull { document -> document.toAdminUserDto()?.toDomain(document.id) }
                    .orEmpty()

                trySend(users)
            }

        awaitClose {
            registration.remove()
        }
    }

    // Lắng nghe realtime danh sách drivers để admin quản lý tài xế.
    fun observeDrivers(): Flow<List<AdminDriver>> = callbackFlow {
        val registration = firestore.collection("drivers")
            .limit(200)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val drivers = snapshot?.documents
                    ?.mapNotNull { document -> document.toAdminDriverDto()?.toDomain(document.id) }
                    .orEmpty()

                trySend(drivers)
            }

        awaitClose {
            registration.remove()
        }
    }

    // Lưu user vào users, nếu role DRIVER thì tạo/cập nhật thêm hồ sơ driver.
    suspend fun saveUser(user: AdminUser) {
        val now = System.currentTimeMillis()
        val document = if (user.id.isBlank()) {
            firestore.collection("users").document()
        } else {
            firestore.collection("users").document(user.id)
        }

        val savedUser = user.copy(
            id = document.id,
            role = user.role.uppercase().ifBlank { "USER" },
            isActive = user.isActive,
            updatedAt = now,
            createdAt = if (user.createdAt == 0L) now else user.createdAt
        )
        document.set(savedUser.toDto(), SetOptions.merge()).await()

        if (savedUser.role == "DRIVER") {
            upsertDriverFromUser(savedUser, now)
        }
    }

    // Xoá hồ sơ user khỏi collection users.
    suspend fun deleteUser(userId: String) {
        if (userId.isBlank()) return
        firestore.collection("users")
            .document(userId)
            .delete()
            .await()
    }

    // Lưu hồ sơ tài xế vào drivers và đồng bộ role DRIVER sang users.
    suspend fun saveDriver(driver: AdminDriver) {
        val now = System.currentTimeMillis()
        val document = if (driver.id.isBlank()) {
            firestore.collection("drivers").document()
        } else {
            firestore.collection("drivers").document(driver.id)
        }

        val savedDriver = driver.copy(
            id = document.id,
            userId = driver.userId.ifBlank { driver.id.ifBlank { document.id } },
            updatedAt = now,
            createdAt = if (driver.createdAt == 0L) now else driver.createdAt
        )
        document.set(savedDriver.toDto(), SetOptions.merge()).await()
        syncDriverUserRole(savedDriver, now)
    }

    // Xoá hồ sơ tài xế khỏi collection drivers.
    suspend fun deleteDriver(driverId: String) {
        if (driverId.isBlank()) return
        firestore.collection("drivers")
            .document(driverId)
            .delete()
            .await()
    }

    // Tạo hoặc cập nhật driver từ user khi admin đổi role sang DRIVER.
    private suspend fun upsertDriverFromUser(
        user: AdminUser,
        now: Long
    ) {
        val driverDocument = firestore.collection("drivers").document(user.id)
        val existing = driverDocument.get().await()
        val existingDriver = existing.toAdminDriverDto()?.toDomain(existing.id)

        val driver = (existingDriver ?: AdminDriver()).copy(
            id = user.id,
            userId = user.id,
            name = user.name,
            phone = user.phone,
            email = user.email,
            isActive = user.isActive,
            updatedAt = now,
            createdAt = existingDriver?.createdAt?.takeIf { it != 0L } ?: now
        )

        saveDriver(driver)
    }

    // Đồng bộ thông tin driver ngược về users để phân quyền và profile đúng.
    private suspend fun syncDriverUserRole(
        driver: AdminDriver,
        now: Long
    ) {
        val userDocumentId = driver.userId.ifBlank { driver.id }
        if (userDocumentId.isBlank()) return

        val userDocument = firestore.collection("users").document(userDocumentId)
        val existing = userDocument.get().await()
        if (!existing.exists()) return

        val data = mapOf(
            "name" to driver.name,
            "phone" to driver.phone,
            "email" to driver.email,
            "role" to "DRIVER",
            "active" to driver.isActive,
            "updatedAt" to now
        )

        userDocument.set(data, SetOptions.merge()).await()
    }
}
