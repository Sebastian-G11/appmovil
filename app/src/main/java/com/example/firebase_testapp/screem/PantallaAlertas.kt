package com.example.firebase_testapp.screem

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

data class AlertaIoT(
    val id: String = "",
    val tipo: String = "",
    val valor: Int = 0,
    val timestamp: Long = 0L,
    val mensaje: String = ""
)

@Composable
fun PantallaAlertas() {

    val dbRef = remember { FirebaseDatabase.getInstance().getReference("alertas_iot") }
    var alertas by remember { mutableStateOf<List<AlertaIoT>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        dbRef.orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lista = mutableListOf<AlertaIoT>()
                    snapshot.children.forEach { child ->
                        val alerta = AlertaIoT(
                            id = child.key ?: "",
                            tipo = child.child("tipo").getValue(String::class.java) ?: "",
                            valor = child.child("valor").getValue(Int::class.java) ?: 0,
                            timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0L,
                            mensaje = child.child("mensaje").getValue(String::class.java) ?: ""
                        )
                        lista.add(alerta)
                    }
                    alertas = lista.sortedByDescending { it.timestamp }
                    isLoading = false
                }

                override fun onCancelled(error: DatabaseError) {
                    isLoading = false
                }
            })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C141A))
            .systemBarsPadding()
            .padding(24.dp)
    ) {
        Text(
            text = "Historial de Alertas IoT",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF23A8F2))
                }
            }

            alertas.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(R.drawable.notification),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay alertas registradas",
                            color = Color(0xFF8FA0AB),
                            fontSize = 16.sp
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(alertas) { alerta ->
                        AlertaCard(alerta)
                    }
                }
            }
        }
    }
}

@Composable
fun AlertaCard(alerta: AlertaIoT) {
    val iconoAlerta = when (alerta.tipo) {
        "humedad_alta" -> R.drawable.humedad
        "humedad_baja" -> R.drawable.humedad
        "temperatura_alta" -> R.drawable.sun
        "viento_fuerte" -> R.drawable.wind
        else -> R.drawable.notification
    }

    val colorTipo = when (alerta.tipo) {
        "humedad_alta", "temperatura_alta", "viento_fuerte" -> Color(0xFFFF5252)
        "humedad_baja" -> Color(0xFFFFA726)
        else -> Color(0xFF23A8F2)
    }

    val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        .format(Date(alerta.timestamp))

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2A33)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(iconoAlerta),
                contentDescription = null,
                tint = colorTipo,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alerta.mensaje,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = fecha,
                    color = Color(0xFF8FA0AB),
                    fontSize = 12.sp
                )
            }

            Text(
                text = "${alerta.valor}%",
                color = colorTipo,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}