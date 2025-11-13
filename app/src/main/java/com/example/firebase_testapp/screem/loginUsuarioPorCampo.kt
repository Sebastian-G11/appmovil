package com.example.firebase_testapp.screem

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

fun loginUsuarioPorCampo(
    username: String,
    password: String,
    onResult: (Boolean, String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    db.collection("usuarios")
        .whereEqualTo("user", username)
        .whereEqualTo("password", password)
        .get()
        .addOnSuccessListener { result ->
            if (!result.isEmpty) {
                Log.d("FirestoreLogin", "✅ Usuario encontrado: $username")
                onResult(true, "Inicio de sesión exitoso como $username")
            } else {
                onResult(false, "Usuario o contraseña incorrectos")
            }
        }
        .addOnFailureListener { e ->
            Log.e("FirestoreLogin", "❌ Error: ${e.message}")
            onResult(false, "Error de conexión con Firestore")
        }
}

