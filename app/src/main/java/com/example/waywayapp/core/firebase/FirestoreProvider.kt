package com.example.waywayapp.core.firebase

import com.example.waywayapp.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

object FirestoreProvider {
    val db: FirebaseFirestore by lazy {
        val databaseId = BuildConfig.FIRESTORE_DATABASE_ID.trim()

        if (databaseId.isBlank() || databaseId == "(default)") {
            Firebase.firestore
        } else {
            FirebaseFirestore.getInstance(databaseId)
        }
    }
}
