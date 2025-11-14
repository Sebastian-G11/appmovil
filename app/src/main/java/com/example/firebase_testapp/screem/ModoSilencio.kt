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
import java.util.*

@Composable
fun ModoSilencio() {
    val context = LocalContext.current

    val (datos) = LeerFirebase("modo_silencio", Map::class.java)

    var modoNoche by remember { mutableStateOf(false) }
    var alertasCriticas by remember { mutableStateOf(false) }
    var desde by remember { mutableStateOf("23:00") }
    var hasta by remember { mutableStateOf("07:00") }

    LaunchedEffect(datos) {
        if (datos != null) {
            modoNoche = (datos["activo"] as? Boolean) ?: false
            alertasCriticas = (datos["alertas_criticas"] as? Boolean) ?: false
            desde = (datos["desde"] as? String) ?: "23:00"
            hasta = (datos["hasta"] as? String) ?: "07:00"
        }
    }

    fun actualizarFirebase() {
        escribirFirebase("modo_silencio", mapOf(
            "activo" to modoNoche,
            "alertas_criticas" to alertasCriticas,
            "desde" to desde,
            "hasta" to hasta
        ))
    }

    fun seleccionarHora(inicio: Boolean) {
        val c = Calendar.getInstance()
        TimePickerDialog(context, { _, h, m ->
            val hora = "%02d:%02d".format(h, m)
            if (inicio) desde = hora else hasta = hora
            actualizarFirebase()
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())   // SCROLL AGREGADO
            .background(Color(0xFF0C141A))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text("Modo Silencio", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Text(
            "Reduce las interrupciones durante la noche.",
            color = Color(0xFF8FA0AB),
            fontSize = 14.sp
        )

        ItemSwitch(
            "Activar Modo Noche",
            null,
            modoNoche
        ) { modoNoche = it; actualizarFirebase() }

        Text(
            "Horario Programado",
            color = Color(0xFFB0B8C1),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ItemHora("Desde", "Define la hora de inicio", desde) { seleccionarHora(true) }
            ItemHora("Hasta", "Define la hora de fin", hasta) { seleccionarHora(false) }
        }

        Text(
            "Excepciones de Silencio",
            color = Color(0xFFB0B8C1),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        ItemSwitch(
            "Permitir Alertas Críticas",
            "Viento, lluvia fuerte, etc.",
            alertasCriticas
        ) { alertasCriticas = it; actualizarFirebase() }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF12212B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Silencio activado de $desde a $hasta. Solo se permiten alertas críticas.",
                color = Color(0xFF23A8F2),
                fontSize = 13.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}



@Composable
private fun ItemSwitch(titulo: String, desc: String?, valor: Boolean, onChange: (Boolean) -> Unit) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                desc?.let {
                    Text(
                        it,
                        color = Color(0xFF8FA0AB),
                        fontSize = 13.sp
                    )
                }
            }

            Switch(
                checked = valor,
                onCheckedChange = onChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF23A8F2),
                    uncheckedThumbColor = Color(0xFFB0B8C1)
                )
            )
        }
    }
}

@Composable
private fun ItemHora(titulo: String, desc: String, hora: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2A33)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(desc, color = Color(0xFF8FA0AB), fontSize = 13.sp)
            }

            Text(hora, color = Color(0xFF23A8F2), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
