package com.example.firebase_testapp.loginLogic

import com.google.firebase.database.FirebaseDatabase

fun registrarAdmin() {
    val db = FirebaseDatabase.getInstance().getReference("usuarios")
    val adminRef = db.child("admin")

    adminRef.get().addOnSuccessListener {
        if (!it.exists()) {
            val adminData = mapOf(
                "user" to "admin",
                // Se usa la misma función hash definida en loginUsuarioRealtime.kt
                "password" to hashPassword("1234")
            )
            adminRef.setValue(adminData)
        }
    }
}
