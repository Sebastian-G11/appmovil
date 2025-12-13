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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun ModoTecho() {

    // Se leen los valores desde Firebase usando el helper genérico.
    // LeerFirebase devuelve un Triple (valor, loading, error),
    // por lo que se desestructura explícitamente para evitar recomposiciones erráticas.
    val (modoActualRaw, _, _) =
        LeerFirebase("techo/modo", String::class.java)

    val (estadoTechoRaw, _, _) =
        LeerFirebase("techo/estado", String::class.java)

    // Se asignan valores por defecto para evitar null durante recomposición
    // cuando Firebase aún no ha respondido.
    val modoActual = modoActualRaw ?: "Desconocido"
    val estadoTecho = estadoTechoRaw ?: "Desconocido"

    // Contexto necesario para acceder a DataStore
    val context = LocalContext.current

    // Scope para lanzar corrutinas desde eventos UI
    val scope = rememberCoroutineScope()

    // Mensaje local solo para feedback visual al usuario
    var mensajeComando by remember { mutableStateOf("") }

    // Escritura directa del modo en Firebase
    fun actualizarModo(nuevoModo: String) {
        escribirFirebase("techo/modo", nuevoModo)
    }

    // Escritura protegida del estado del techo
    // Si el estado ya es el mismo, se evita escribir nuevamente
    // para cortar bucles de escritura Firebase -> Compose -> Firebase.
    fun actualizarEstado(nuevoEstado: String) {
        if (estadoTechoRaw == nuevoEstado) return
        escribirFirebase("techo/estado", nuevoEstado)
    }

    /*
     Reenvío automático de comandos guardados localmente.
     Este bloque se ejecuta cuando Firebase vuelve a entregar datos
     (por ejemplo, al reconectar a internet).

     - Lee el último comando almacenado en DataStore
     - Si es distinto al estado actual recibido desde Firebase,
       lo reenvía una sola vez.
     */
    LaunchedEffect(estadoTechoRaw) {
        if (estadoTechoRaw != null) {
            val prefs = context.sessionDataStore.data.first()
            val ultimoComando = prefs[stringPreferencesKey("last_comando")]
            if (ultimoComando != null && ultimoComando != estadoTechoRaw) {
                actualizarEstado(ultimoComando)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C141A))
            .systemBarsPadding()   // ← CAMBIO CLAVE
            .padding(24.dp),
        contentAlignment = Alignment.TopCenter
    )
    {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Selecciona el modo del techo",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            // Selector de modo automático
            ModoCard(
                titulo = "Automático",
                descripcion = "El techo se abrirá y cerrará según el clima y la hora.",
                icono = R.drawable.auto,
                seleccionado = modoActual == "Automático",
                onClick = { actualizarModo("Automático") }
            )

            // Selector de modo manual
            ModoCard(
                titulo = "Manual",
                descripcion = "Controla manualmente el techo.",
                icono = R.drawable.manual,
                seleccionado = modoActual == "Manual",
                onClick = { actualizarModo("Manual") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            /*
             El panel manual solo se muestra si:
             - El modo es Manual
             - Firebase ya entregó un estado válido (estadoTechoRaw != null)

             Esto evita ejecutar acciones sobre un estado inexistente.
             */
            if (modoActual == "Manual" && estadoTechoRaw != null) {
                EstadoTechoPanel(
                    estado = estadoTecho,
                    onAbrir = {
                        // Se guarda el comando localmente para modo offline
                        scope.launch {
                            context.sessionDataStore.edit {
                                it[stringPreferencesKey("last_comando")] = "Abierto"
                            }
                        }
                        actualizarEstado("Abierto")
                        mensajeComando = "Comando enviado al dispositivo"
                    },
                    onCerrar = {
                        scope.launch {
                            context.sessionDataStore.edit {
                                it[stringPreferencesKey("last_comando")] = "Cerrado"
                            }
                        }
                        actualizarEstado("Cerrado")
                        mensajeComando = "Comando enviado al dispositivo"
                    }
                )
            }

            // Indicador visual cuando Firebase aún no responde (modo local)
            if (estadoTechoRaw == null) {
                Text(
                    text = "Modo local activo. Se sincronizará al reconectar.",
                    color = Color.Yellow,
                    fontSize = 14.sp
                )
            }

            // Feedback simple al usuario tras enviar un comando
            if (mensajeComando.isNotEmpty()) {
                Text(
                    text = mensajeComando,
                    color = Color.Green,
                    fontSize = 14.sp
                )
            }
        }
    }
}

/* ======================================================
   COMPONENTES UI
   Se mantienen en el mismo archivo para evitar
   errores de referencia no resuelta.
   No contienen lógica de negocio.
   ====================================================== */

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
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = icono),
                contentDescription = titulo,
                tint = if (seleccionado) Color(0xFF23A8F2) else Color(0xFFB0B8C1),
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    color = Color.White,
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
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color(0xFF23A8F2),
                    unselectedColor = Color(0xFFB0B8C1)
                )
            )
        }
    }
}

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

        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            ControlButton("Abrir", R.drawable.up, onAbrir)
            ControlButton("Cerrar", R.drawable.down, onCerrar)
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