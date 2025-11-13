package com.example.firebase_testapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.google.firebase.database.FirebaseDatabase

@Composable
fun ModoTecho() {
    val databaseModo = FirebaseDatabase.getInstance().getReference("techo/modo")
    val databaseEstado = FirebaseDatabase.getInstance().getReference("techo/estado")

    var modoActual by remember { mutableStateOf<String?>(null) }
    var estadoTecho by remember { mutableStateOf<String?>(null) }

    // Escuchar cambios de modo y estado desde Firebase
    LaunchedEffect(Unit) {
        databaseModo.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                modoActual = snapshot.getValue(String::class.java)
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })

        databaseEstado.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                estadoTecho = snapshot.getValue(String::class.java)
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })
    }

    fun actualizarModo(nuevoModo: String) {
        databaseModo.setValue(nuevoModo)
    }

    fun actualizarEstado(nuevoEstado: String) {
        databaseEstado.setValue(nuevoEstado)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C141A))
            .padding(24.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Selecciona el modo del techo",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            // Tarjeta Automático
            ModoCard(
                titulo = "Automático",
                descripcion = "El techo se abrirá y cerrará según el clima y la hora.",
                icono = R.drawable.auto,
                seleccionado = modoActual == "Automático",
                onClick = { actualizarModo("Automático") }
            )

            // Tarjeta Manual
            ModoCard(
                titulo = "Manual",
                descripcion = "Controla manualmente el techo.",
                icono = R.drawable.manual,
                seleccionado = modoActual == "Manual",
                onClick = { actualizarModo("Manual") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 🧭 Panel Manual (solo visible si está en modo manual)
            if (modoActual == "Manual") {
                EstadoTechoPanel(
                    estado = estadoTecho ?: "Desconocido",
                    onAbrir = { actualizarEstado("Abierto") },
                    onCerrar = { actualizarEstado("Cerrado") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))


        }
    }
}

@Composable
fun ModoCard(
    titulo: String,
    descripcion: String,
    icono: Int,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    val bordeColor = if (seleccionado) Color(0xFF23A8F2) else Color.Transparent
    val fondoColor = if (seleccionado) Color(0xFF111B22) else Color(0xFF1C2A33)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = fondoColor),
        border = BorderStroke(2.dp, bordeColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                painter = painterResource(id = icono),
                contentDescription = titulo,
                tint = if (seleccionado) Color(0xFF23A8F2) else Color(0xFFB0B8C1),
                modifier = Modifier.size(32.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    color = if (seleccionado) Color.White else Color(0xFFB0B8C1),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = descripcion,
                    color = Color(0xFF8FA0AB),
                    fontSize = 13.sp
                )
            }

            RadioButton(
                selected = seleccionado,
                onClick = { onClick() },
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color(0xFF23A8F2),
                    unselectedColor = Color(0xFFB0B8C1)
                )
            )
        }
    }
}

// 🌤 Panel de control manual del techo
@Composable
fun EstadoTechoPanel(
    estado: String,
    onAbrir: () -> Unit,
    onCerrar: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Estado del Techo",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = estado,
            color = Color(0xFF23A8F2),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            ControlButton(
                texto = "Abrir",
                icono = R.drawable.up, // usa tu ícono de abrir
                onClick = onAbrir
            )
            ControlButton(
                texto = "Cerrar",
                icono = R.drawable.down, // usa tu ícono de cerrar
                onClick = onCerrar
            )
        }
    }
}

@Composable
fun ControlButton(
    texto: String,
    icono: Int,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .size(width = 140.dp, height = 100.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2A33))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = icono),
                contentDescription = texto,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(texto, color = Color.White, fontSize = 16.sp)
        }
    }
}
