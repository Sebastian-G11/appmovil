package com.example.firebase_testapp

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.*
import java.util.*

@Composable
fun ProgramacionHoraria() {

    val context = LocalContext.current
    val dbRef = remember { FirebaseDatabase.getInstance().getReference("programacion_horaria") }

    var desde by remember { mutableStateOf("--:--") }
    var hasta by remember { mutableStateOf("--:--") }
    var estado by remember { mutableStateOf("deshabilitado") }

    LaunchedEffect(true) {
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                desde = snapshot.child("desde").getValue(String::class.java) ?: "--:--"
                hasta = snapshot.child("hasta").getValue(String::class.java) ?: "--:--"
                estado = snapshot.child("estado").getValue(String::class.java) ?: "deshabilitado"
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun actualizarFirebase() {
        dbRef.updateChildren(
            mapOf(
                "desde" to desde,
                "hasta" to hasta,
                "estado" to estado
            )
        )
    }

    fun seleccionarHora(inicio: Boolean) {
        if (estado == "deshabilitado") return

        val c = Calendar.getInstance()
        TimePickerDialog(
            context,
            { _, h, m ->
                val hora = "%02d:%02d".format(h, m)
                if (inicio) desde = hora else hasta = hora
                actualizarFirebase()
            },
            c.get(Calendar.HOUR_OF_DAY),
            c.get(Calendar.MINUTE),
            true
        ).show()
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFF0C141A))
            .systemBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        Text(
            "Horario Programado",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2A33)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Estado", color = Color.White, fontSize = 16.sp)
                    Text(
                        "Activa o desactiva la programación",
                        color = Color(0xFF8FA0AB),
                        fontSize = 13.sp
                    )
                }

                Switch(
                    checked = estado == "habilitado",
                    onCheckedChange = {
                        estado = if (it) "habilitado" else "deshabilitado"
                        actualizarFirebase()
                    }
                )
            }
        }

        ItemHora(
            "Desde",
            "Define la hora de inicio",
            desde,
            estado == "habilitado"
        ) { seleccionarHora(true) }

        ItemHora(
            "Hasta",
            "Define la hora de fin",
            hasta,
            estado == "habilitado"
        ) { seleccionarHora(false) }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF12212B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "El techo se cerrará de $desde a $hasta.",
                color = Color(0xFF23A8F2),
                fontSize = 13.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun ItemHora(
    titulo: String,
    desc: String,
    hora: String,
    habilitado: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val fondo = if (habilitado) Color(0xFF1C2A33) else Color(0xFF2A2A2A)
    val textoColor = if (habilitado) Color.White else Color(0xFF777777)
    val horaColor = if (habilitado) Color(0xFF23A8F2) else Color(0xFF555555)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = fondo),
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = habilitado) { onClick() }
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(titulo, color = textoColor, fontSize = 16.sp)
                Text(desc, color = textoColor.copy(alpha = 0.6f), fontSize = 13.sp)
            }

            Text(hora, color = horaColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
