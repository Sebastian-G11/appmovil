import com.google.firebase.database.FirebaseDatabase

fun registrarAdmin() {
    val db = FirebaseDatabase.getInstance().getReference("usuarios")
    val adminRef = db.child("admin")

    adminRef.get().addOnSuccessListener {
        if (!it.exists()) {
            val adminData = mapOf(
                "user" to "admin",
                "password" to "1234",
            )
            adminRef.setValue(adminData)
        }
    }
}
