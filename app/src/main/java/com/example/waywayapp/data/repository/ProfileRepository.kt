package com.example.waywayapp.data.repository

import com.example.waywayapp.core.firebase.FirestoreProvider
import com.example.waywayapp.data.model.AdminDriver
import com.example.waywayapp.data.model.AdminUser
import com.example.waywayapp.data.remote.dto.firestore.toAdminDriverDto
import com.example.waywayapp.data.remote.dto.firestore.toAdminUserDto
import com.example.waywayapp.data.remote.dto.firestore.toDto
import com.google.firebase.Firebase
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

// Repository đọc/ghi hồ sơ cá nhân của user, admin và driver.
class ProfileRepository {
    // Firebase Auth cung cấp uid và profile cơ bản của tài khoản hiện tại.
    private val auth = Firebase.auth
    // Firestore lưu profile mở rộng trong users và drivers.
    private val firestore = FirestoreProvider.db

    // UID hiện tại dùng để truy cập document users/{uid} và drivers/{uid}.
    val currentUid: String?
        get() = auth.currentUser?.uid

    // Tải hồ sơ user từ Firestore, thiếu field nào thì fallback từ Firebase Auth.
    suspend fun loadUserProfile(): AdminUser {
        val user = requireNotNull(auth.currentUser)
        val document = firestore.collection("users")
            .document(user.uid)
            .get()
            .await()

        val profile = document.toAdminUserDto()?.toDomain(document.id)
        return AdminUser(
            id = user.uid,
            name = profile?.name?.ifBlank { user.displayName.orEmpty() }
                ?: user.displayName.orEmpty(),
            email = profile?.email?.ifBlank { user.email.orEmpty() }
                ?: user.email.orEmpty(),
            phone = profile?.phone?.ifBlank { user.phoneNumber.orEmpty() }
                ?: user.phoneNumber.orEmpty(),
            role = profile?.role ?: "USER",
            avatarUrl = profile?.avatarUrl?.ifBlank { user.photoUrl?.toString().orEmpty() }
                ?: user.photoUrl?.toString().orEmpty(),
            momoLinked = profile?.momoLinked ?: false,
            momoPhone = profile?.momoPhone.orEmpty(),
            momoLinkedAt = profile?.momoLinkedAt ?: 0L,
            isActive = profile?.isActive ?: true,
            updatedAt = profile?.updatedAt ?: 0L,
            createdAt = profile?.createdAt ?: 0L
        )
    }

    // Tải hồ sơ driver nếu user hiện tại có role DRIVER.
    suspend fun loadDriverProfile(): AdminDriver? {
        val uid = currentUid ?: return null
        val document = firestore.collection("drivers")
            .document(uid)
            .get()
            .await()

        if (!document.exists()) return null

        return document.toAdminDriverDto()?.toDomain(document.id)
    }

    // Lưu hồ sơ user lên users và cập nhật displayName trong Firebase Auth.
    suspend fun saveUserProfile(
        profile: AdminUser
    ) {
        val user = requireNotNull(auth.currentUser)
        val now = System.currentTimeMillis()
        val savedProfile = profile.copy(
            id = user.uid,
            role = profile.role.uppercase().ifBlank { "USER" },
            updatedAt = now,
            createdAt = if (profile.createdAt == 0L) now else profile.createdAt
        )

        if (savedProfile.name.isNotBlank()) {
            user.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setDisplayName(savedProfile.name)
                    .build()
            ).await()
        }

        firestore.collection("users")
            .document(user.uid)
            .set(savedProfile.toDto(), SetOptions.merge())
            .await()
    }

    // Lưu phần thông tin tài xế vào drivers/{uid}.
    suspend fun saveDriverProfile(
        profile: AdminDriver
    ) {
        val uid = currentUid ?: return
        val now = System.currentTimeMillis()
        val savedProfile = profile.copy(
            id = uid,
            userId = profile.userId.ifBlank { uid },
            updatedAt = now,
            createdAt = if (profile.createdAt == 0L) now else profile.createdAt
        )

        firestore.collection("drivers")
            .document(uid)
            .set(savedProfile.toDto(), SetOptions.merge())
            .await()
    }

    // Đăng xuất tài khoản hiện tại.
    fun signOut() {
        auth.signOut()
    }
}
