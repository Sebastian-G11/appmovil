package com.example.firebase_testapp

import android.util.Log
import com.google.firebase.database.FirebaseDatabase

fun escribirFirebase(field: String, value: Any) {
    try {
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference(field)
        ref.setValue(value)
            .addOnSuccessListener {
                Log.d("FirebaseWrite", "✅ Datos guardados en '$field'")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseWrite", "❌ Error al guardar datos: ${e.message}", e)
            }
    } catch (e: Exception) {
        Log.e("FirebaseWrite", "⚠️ Excepción: ${e.message}", e)
    }
}
