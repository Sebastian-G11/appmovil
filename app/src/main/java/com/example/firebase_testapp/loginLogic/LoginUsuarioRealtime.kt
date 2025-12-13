package com.example.firebase_testapp.loginLogic

import android.util.Log
import com.google.firebase.database.*
import java.security.MessageDigest

fun loginUsuarioRealtime(
    username: String,
    password: String,
    onResult: (Boolean, String) -> Unit
) {
    val dbRef = FirebaseDatabase.getInstance().getReference("usuarios")

    val hashedInputPassword = hashPassword(password)

    dbRef.addListenerForSingleValueEvent(object : ValueEventListener {

        override fun onDataChange(snapshot: DataSnapshot) {
            var found = false

            for (userSnap in snapshot.children) {
                val user = userSnap.child("user").getValue(String::class.java)
                val pass = userSnap.child("password").getValue(String::class.java)

                if (user == null || pass == null) continue

                if (user == username && pass == hashedInputPassword) {
                    found = true
                    onResult(true, "Inicio de sesión exitoso")
                    break
                }
            }

            if (!found) {
                onResult(false, "Usuario o contraseña incorrectos")
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("Login", error.message)
            onResult(false, "Sin conexión, reintentando...")
        }
    })
}

/*
 Hash SHA-256.
 Evita almacenar contraseñas en texto plano.
 Implementación simple para evaluación académica.
*/
fun hashPassword(password: String): String {
    val bytes = MessageDigest
        .getInstance("SHA-256")
        .digest(password.toByteArray())

    val result = StringBuilder()
    for (b in bytes) {
        result.append(String.format("%02x", b))
    }
    return result.toString()
}
