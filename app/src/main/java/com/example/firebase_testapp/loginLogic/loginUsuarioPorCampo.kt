package com.example.firebase_testapp.loginLogic

import android.util.Log
import com.google.firebase.database.*

fun loginUsuarioRealtime(
    username: String,
    password: String,
    onResult: (Boolean, String) -> Unit
) {
    val dbRef = FirebaseDatabase.getInstance().getReference("usuarios")
    //conecta con la ruta usuarios en la base de datos boeee

    dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val t = object : GenericTypeIndicator<Map<String, Any>>() {}
            var found = false

            for (userSnap in snapshot.children) {
                val data = userSnap.getValue(t)
                val user = data?.get("user")?.toString()
                val pass = data?.get("password")?.toString()//aqui lee los datos

                if (user == username && pass == password) {
                    found = true
                    Log.d("RealtimeLogin", "Usuario encontrado: $username")
                    onResult(true, "Inicio de sesión exitoso")
                    break
                }
            }

            if (!found) onResult(false, "Usuario o contraseña incorrectos")
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("RealtimeLogin", "❌ Error: ${error.message}")
            onResult(false, "Error de conexión con la base de datos")
        }
    })
}
