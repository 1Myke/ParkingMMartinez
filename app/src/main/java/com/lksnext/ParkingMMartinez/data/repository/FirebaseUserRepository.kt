package com.lksnext.ParkingMMartinez.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.lksnext.ParkingMMartinez.model.User
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseUserRepository : UserRepository {
    private val auth = FirebaseAuth.getInstance()

    override suspend fun registerUser(user: User): Boolean {
        return try {
            // 1. Crear usuario en Auth
            val result = auth.createUserWithEmailAndPassword(user.email, user.pass).await()
            val userId = result.user?.uid ?: throw Exception("Auth fallida")

            // 2. Guardar en Firestore
            val db = FirebaseFirestore.getInstance()
            val userData = hashMapOf(
                "name" to user.name,
                "lastName" to user.lastName,
                "username" to user.username,
                "email" to user.email,
                "avatarURL" to user.avatarURL
            )

            // Añadimos un log para verificar que llegamos aquí
            println("DEBUG_FIREBASE: Intentando escribir en Firestore para $userId")
            db.collection("users").document(userId).set(userData).await()
            println("DEBUG_FIREBASE: Registro en Firestore exitoso")

            true
        } catch (e: Exception) {
            // MUY IMPORTANTE: Imprimimos el error real aquí
            println("DEBUG_FIREBASE_ERROR: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    override suspend fun authenticate(email: String, pass: String): User? {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            result.user?.let { firebaseUser ->
                User(id = firebaseUser.uid, email = firebaseUser.email ?: "")
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getUserById(userId: String): User? {
        return try {
            val db = FirebaseFirestore.getInstance()
            val snapshot = db.collection("users").document(userId).get().await()

            if (snapshot.exists()) {
                User(
                    id = userId,
                    name = snapshot.getString("name") ?: "",
                    lastName = snapshot.getString("lastName") ?: "",
                    username = snapshot.getString("username") ?: "",
                    email = snapshot.getString("email") ?: "",
                    avatarURL = snapshot.getString("avatarURL")
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateAvatar(userId: String, url: String): Boolean {
        return try {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(userId).update("avatarURL", url).await()
            true
        } catch (e: Exception) {
            println("DEBUG_FIRESTORE_PATCH_ERROR: ${e.message}")
            false
        }
    }
}