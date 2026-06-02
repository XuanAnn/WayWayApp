package com.example.waywayapp.data.repository

import com.example.waywayapp.BuildConfig
import com.example.waywayapp.core.firebase.FirestoreProvider
import com.example.waywayapp.data.model.AdminUser
import com.example.waywayapp.data.model.AuthUser
import com.example.waywayapp.data.remote.dto.firestore.toAdminUserDto
import com.example.waywayapp.data.remote.dto.firestore.toDto
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Repository gom toàn bộ thao tác Firebase Auth và đồng bộ hồ sơ users trên Firestore.
class FirebaseAuthRepository(
    private val auth: FirebaseAuth = Firebase.auth
) {
    // Firestore lưu profile, role, active và bảng phoneUsers chống trùng số.
    private val firestore = FirestoreProvider.db
    // Scope nền dùng để upsert profile mà không chặn luồng đăng nhập.
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Firebase user hiện tại, dùng để lấy uid/role sau đăng nhập.
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    // Đăng nhập email/password rồi kiểm tra active và role trong Firestore.
    suspend fun signInWithEmail(
        email: String,
        password: String
    ): AuthUser {
        val result = auth
            .signInWithEmailAndPassword(email, password)
            .await()
        val user = requireNotNull(result.user)
        val authUser = user.toAuthUser().withStoredRole()
        ensureUserIsActive(user.uid)
        upsertUserProfileAsync(authUser)
        return authUser
    }

    // Đăng ký email/password trực tiếp và tạo hồ sơ users mặc định.
    suspend fun registerWithEmail(
        email: String,
        password: String,
        name: String?,
        phone: String?
    ): AuthUser {
        val result = auth
            .createUserWithEmailAndPassword(email, password)
            .await()
        val user = requireNotNull(result.user)
        if (!name.isNullOrBlank()) {
            user.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setDisplayName(name.trim())
                    .build()
            ).await()
        }
        val authUser = user.toAuthUser(name = name, phone = phone).withStoredRole()
        upsertUserProfileAsync(authUser, activateNewUser = true)
        return authUser
    }

    // Gửi email reset password của Firebase cho chức năng quên mật khẩu.
    suspend fun sendPasswordResetEmail(
        email: String
    ) {
        auth.sendPasswordResetEmail(email).await()
    }

    // Đăng nhập bằng OTP và kiểm tra số điện thoại không bị gắn với uid khác.
    suspend fun signInWithPhoneCredential(
        credential: PhoneAuthCredential
    ): AuthUser {
        val result = auth.signInWithCredential(credential).await()
        val user = requireNotNull(result.user)
        val authUser = user.toAuthUser().withStoredRole()
        ensurePhoneIsAvailableForUser(authUser.phone, user.uid)
        ensureUserIsActive(user.uid)
        upsertUserProfileAsync(authUser)
        return authUser
    }

    // Xác thực OTP ở bước đăng ký, chưa lưu profile đầy đủ cho tới khi nhập thông tin.
    suspend fun verifyPhoneCredentialForRegistration(
        credential: PhoneAuthCredential
    ): AuthUser {
        val result = auth.signInWithCredential(credential).await()
        val user = requireNotNull(result.user)
        val authUser = user.toAuthUser().withStoredRole()
        ensurePhoneCanRegister(authUser.phone, user.uid)
        return authUser
    }

    // Link email/password vào tài khoản phone và lưu hồ sơ user hoàn chỉnh.
    suspend fun completePhoneRegistration(
        email: String,
        password: String,
        name: String,
        age: String,
        phone: String
    ): AuthUser {
        val user = requireNotNull(currentUser) {
            "Vui lòng xác thực OTP trước."
        }
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()

        if (user.email.isNullOrBlank()) {
            val credential = EmailAuthProvider.getCredential(trimmedEmail, password)
            user.linkWithCredential(credential).await()
        }

        user.updateProfile(
            UserProfileChangeRequest.Builder()
                .setDisplayName(trimmedName)
                .build()
        ).await()

        val authUser = user.toAuthUser(
            name = trimmedName,
            phone = phone
        ).copy(email = trimmedEmail).withStoredRole()
        upsertUserProfile(authUser, age = age, activateNewUser = true)
        return authUser
    }

    // Đăng nhập Google bằng idToken rồi đồng bộ profile lên Firestore.
    suspend fun signInWithGoogleIdToken(
        idToken: String
    ): AuthUser {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val user = requireNotNull(result.user)
        val authUser = user.toAuthUser().withStoredRole()
        ensureUserIsActive(user.uid)
        upsertUserProfileAsync(authUser)
        return authUser
    }

    // Đăng nhập Facebook bằng accessToken rồi đồng bộ profile lên Firestore.
    suspend fun signInWithFacebookAccessToken(
        accessToken: String
    ): AuthUser {
        val credential = FacebookAuthProvider.getCredential(accessToken)
        val result = auth.signInWithCredential(credential).await()
        val user = requireNotNull(result.user)
        val authUser = user.toAuthUser().withStoredRole()
        ensureUserIsActive(user.uid)
        upsertUserProfileAsync(authUser)
        return authUser
    }

    // Đăng xuất khỏi Firebase Auth.
    fun signOut() {
        auth.signOut()
    }

    // Đồng bộ lại profile của user hiện tại khi app mở hoặc cần refresh role.
    suspend fun syncCurrentUserProfile(): AuthUser {
        val user = requireNotNull(currentUser) {
            "Chưa có phiên đăng nhập."
        }
        val authUser = user.toAuthUser().withStoredRole()
        ensureUserIsActive(user.uid)
        upsertUserProfile(authUser)
        return authUser
    }

    // Lấy role từ Firestore để điều hướng đúng màn ADMIN/DRIVER/USER.
    suspend fun getCurrentUserRole(): String {
        val user = currentUser ?: return "GUEST"
        if (isAdminEmail(user.email)) {
            upsertUserProfileAsync(user.toAuthUser().copy(role = "ADMIN"))
            return "ADMIN"
        }

        ensureUserIsActive(user.uid)

        return firestore.collection("users")
            .document(user.uid)
            .get()
            .await()
            .toAdminUserDto()
            ?.role
            ?: "USER"
    }

    // Lấy role nhanh từ email admin khi chưa kịp đọc Firestore.
    fun getFastCurrentUserRole(): String {
        val user = currentUser ?: return "GUEST"
        return if (isAdminEmail(user.email)) {
            "ADMIN"
        } else {
            "USER"
        }
    }

    // Ghi/merge profile user vào users và map phoneUsers để đảm bảo phone là duy nhất.
    private suspend fun upsertUserProfile(
        user: AuthUser,
        age: String = "",
        activateNewUser: Boolean = true
    ) {
        val now = System.currentTimeMillis()
        val document = firestore.collection("users").document(user.uid)
        val existing = document.get().await()
        val existingProfile = existing.toAdminUserDto()?.toDomain(existing.id)
        val normalizedPhone = normalizePhone(user.phone.orEmpty())
        val profile = AdminUser(
            id = user.uid,
            name = user.name,
            age = age.ifBlank { existingProfile?.age.orEmpty() },
            email = user.email.orEmpty(),
            phone = normalizedPhone.ifBlank { user.phone.orEmpty() },
            avatarUrl = user.photoUrl.orEmpty(),
            momoLinked = existingProfile?.momoLinked ?: false,
            momoPhone = existingProfile?.momoPhone.orEmpty(),
            momoLinkedAt = existingProfile?.momoLinkedAt ?: 0L,
            role = user.role,
            isActive = if (existing.exists()) {
                existingProfile?.isActive ?: true
            } else {
                activateNewUser
            },
            updatedAt = now,
            createdAt = existingProfile?.createdAt?.takeIf { it != 0L } ?: now
        )

        // Transaction tránh trường hợp hai tài khoản cùng ghi một số điện thoại.
        firestore.runTransaction { transaction ->
            if (normalizedPhone.isNotBlank()) {
                val phoneDocument = firestore.collection("phoneUsers").document(normalizedPhone)
                val phoneSnapshot = transaction.get(phoneDocument)
                val ownerUid = phoneSnapshot.getString("uid")
                if (phoneSnapshot.exists() && ownerUid != user.uid) {
                    throw IllegalStateException("Số điện thoại đã được sử dụng bởi tài khoản khác.")
                }
                transaction.set(
                    phoneDocument,
                    mapOf(
                        "uid" to user.uid,
                        "phone" to normalizedPhone,
                        "updatedAt" to now,
                        "createdAt" to (phoneSnapshot.getLong("createdAt") ?: now)
                    ),
                    SetOptions.merge()
                )
            }
            transaction.set(document, profile.toDto(), SetOptions.merge())
        }.await()
    }

    // Ghi profile ở background sau đăng nhập mạng xã hội/email.
    private fun upsertUserProfileAsync(
        user: AuthUser,
        activateNewUser: Boolean = true
    ) {
        backgroundScope.launch {
            runCatching {
                upsertUserProfile(user, activateNewUser = activateNewUser)
            }
        }
    }

    // Chặn đăng nhập nếu admin đã chuyển active=false trong Firestore.
    private suspend fun ensureUserIsActive(
        uid: String
    ) {
        val snapshot = firestore.collection("users")
            .document(uid)
            .get()
            .await()

        val active = snapshot.toAdminUserDto()?.toDomain(snapshot.id)?.isActive ?: true

        if (snapshot.exists() && !active) {
            auth.signOut()
            throw IllegalStateException("Tài khoản đã bị khoá. Vui lòng liên hệ quản trị viên.")
        }
    }

    // Kiểm tra số phone đã thuộc uid khác chưa khi đăng nhập bằng OTP.
    private suspend fun ensurePhoneIsAvailableForUser(
        phone: String?,
        uid: String
    ) {
        val normalizedPhone = normalizePhone(phone.orEmpty())
        if (normalizedPhone.isBlank()) return

        val snapshot = firestore.collection("phoneUsers")
            .document(normalizedPhone)
            .get()
            .await()

        val ownerUid = snapshot.getString("uid")
        if (snapshot.exists() && ownerUid != uid) {
            auth.signOut()
            throw IllegalStateException("Số điện thoại đã được sử dụng bởi tài khoản khác.")
        }
    }

    // Kiểm tra ngay sau verify OTP để số đã có tài khoản thì không qua bước nhập info.
    private suspend fun ensurePhoneCanRegister(
        phone: String?,
        uid: String
    ) {
        val normalizedPhone = normalizePhone(phone.orEmpty())
        if (normalizedPhone.isBlank()) return

        val phoneSnapshot = firestore.collection("phoneUsers")
            .document(normalizedPhone)
            .get()
            .await()
        if (phoneSnapshot.exists()) {
            auth.signOut()
            throw IllegalStateException("Số điện thoại đã có tài khoản. Vui lòng đăng nhập.")
        }

        val userSnapshot = firestore.collection("users")
            .document(uid)
            .get()
            .await()
        val existingProfile = userSnapshot.toAdminUserDto()?.toDomain(userSnapshot.id)
        val hasCompletedProfile = !existingProfile?.email.isNullOrBlank() ||
            !existingProfile?.name.isNullOrBlank()
        if (userSnapshot.exists() && hasCompletedProfile) {
            auth.signOut()
            throw IllegalStateException("Số điện thoại đã có tài khoản. Vui lòng đăng nhập.")
        }
    }

    // Map FirebaseUser sang domain AuthUser dùng trong app.
    private fun FirebaseUser.toAuthUser(
        name: String? = displayName,
        phone: String? = phoneNumber
    ): AuthUser {
        return AuthUser(
            uid = uid,
            name = name.orEmpty(),
            email = email,
            phone = phone,
            photoUrl = photoUrl?.toString()
        )
    }

    // Gắn role đã lưu trong users, nếu chưa có thì dùng role mặc định theo email.
    private suspend fun AuthUser.withStoredRole(): AuthUser {
        if (isAdminEmail(email)) {
            return copy(role = "ADMIN")
        }

        val storedRole = firestore.collection("users")
            .document(uid)
            .get()
            .await()
            .toAdminUserDto()
            ?.role
            ?: defaultRoleForEmail(email)
        return copy(role = storedRole)
    }

    // Role mặc định là ADMIN nếu email nằm trong BuildConfig.ADMIN_EMAILS.
    private fun defaultRoleForEmail(
        email: String?
    ): String {
        return if (isAdminEmail(email)) {
            "ADMIN"
        } else {
            "USER"
        }
    }

    // Kiểm tra email có nằm trong danh sách admin cấu hình ở build.gradle không.
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

    // Chuẩn hoá phone để document phoneUsers không bị trùng do khác format.
    private fun normalizePhone(
        value: String
    ): String {
        val raw = value.trim()
            .replace(" ", "")
            .replace("-", "")
            .replace("(", "")
            .replace(")", "")
        return when {
            raw.isBlank() -> ""
            raw.startsWith("+") -> raw
            raw.startsWith("0") -> "+84${raw.drop(1)}"
            else -> raw
        }
    }
}
