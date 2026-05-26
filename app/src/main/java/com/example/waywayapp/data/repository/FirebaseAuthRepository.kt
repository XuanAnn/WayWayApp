package com.example.waywayapp.data.repository

import com.example.waywayapp.BuildConfig
import com.example.waywayapp.core.firebase.FirestoreProvider
import com.example.waywayapp.data.model.AuthUser
import com.google.firebase.Firebase
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = Firebase.auth
) {
    private val firestore = FirestoreProvider.db
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun signInWithEmail(
        email: String,
        password: String
    ): AuthUser {
        val result = auth
            .signInWithEmailAndPassword(email, password)
            .await()
        val user = requireNotNull(result.user)
        val authUser = user.toAuthUser().withStoredRole()
        upsertUserProfileAsync(authUser)
        return authUser
    }

    suspend fun registerWithEmail(
        email: String,
        password: String,
        phone: String?
    ): AuthUser {
        val result = auth
            .createUserWithEmailAndPassword(email, password)
            .await()
        val user = requireNotNull(result.user)
        val authUser = user.toAuthUser(phone = phone).withStoredRole()
        upsertUserProfileAsync(authUser)
        return authUser
    }

    suspend fun signInWithGoogleIdToken(
        idToken: String
    ): AuthUser {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val user = requireNotNull(result.user)
        val authUser = user.toAuthUser().withStoredRole()
        upsertUserProfileAsync(authUser)
        return authUser
    }

    suspend fun signInWithFacebookAccessToken(
        accessToken: String
    ): AuthUser {
        val credential = FacebookAuthProvider.getCredential(accessToken)
        val result = auth.signInWithCredential(credential).await()
        val user = requireNotNull(result.user)
        val authUser = user.toAuthUser().withStoredRole()
        upsertUserProfileAsync(authUser)
        return authUser
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun getCurrentUserRole(): String {
        val user = currentUser ?: return "GUEST"
        if (isAdminEmail(user.email)) {
            upsertUserProfileAsync(user.toAuthUser().copy(role = "ADMIN"))
            return "ADMIN"
        }

        return firestore.collection("users")
            .document(user.uid)
            .get()
            .await()
            .getString("role")
            ?: "USER"
    }

    fun getFastCurrentUserRole(): String {
        val user = currentUser ?: return "GUEST"
        return if (isAdminEmail(user.email)) {
            "ADMIN"
        } else {
            "USER"
        }
    }

    private suspend fun upsertUserProfile(
        user: AuthUser
    ) {
        val now = System.currentTimeMillis()
        val data = mapOf(
            "id" to user.uid,
            "name" to user.name,
            "email" to user.email,
            "phone" to user.phone,
            "avatarUrl" to user.photoUrl,
            "role" to user.role,
            "isActive" to true,
            "updatedAt" to now,
            "createdAt" to now
        )

        firestore.collection("users")
            .document(user.uid)
            .set(data, SetOptions.merge())
            .await()
    }

    private fun upsertUserProfileAsync(
        user: AuthUser
    ) {
        backgroundScope.launch {
            runCatching {
                upsertUserProfile(user)
            }
        }
    }

    private fun FirebaseUser.toAuthUser(
        phone: String? = phoneNumber
    ): AuthUser {
        return AuthUser(
            uid = uid,
            name = displayName.orEmpty(),
            email = email,
            phone = phone,
            photoUrl = photoUrl?.toString()
        )
    }

    private suspend fun AuthUser.withStoredRole(): AuthUser {
        if (isAdminEmail(email)) {
            return copy(role = "ADMIN")
        }

        val storedRole = firestore.collection("users")
            .document(uid)
            .get()
            .await()
            .getString("role")
            ?: defaultRoleForEmail(email)
        return copy(role = storedRole)
    }

    private fun defaultRoleForEmail(
        email: String?
    ): String {
        return if (isAdminEmail(email)) {
            "ADMIN"
        } else {
            "USER"
        }
    }

    private fun isAdminEmail(
        email: String?
    ): Boolean {
        val normalizedEmail = email?.trim()?.lowercase()
            ?: return false
        val adminEmails = BuildConfig.ADMIN_EMAILS
            .split(",")
            .map { it.trim().lowercase() }
            .filter { it.isNotBlank() }

        return normalizedEmail in adminEmails
    }
}
