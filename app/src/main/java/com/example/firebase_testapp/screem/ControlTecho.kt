package com.example.firebase_testapp

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.accompanist.swiperefresh.*
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/* ======================================================
   UTILIDAD RED
   ====================================================== */

/* ======================================================
   PANTALLA PRINCIPAL
   ====================================================== */
@Composable
fun ModoTecho() {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var refreshKey by remember { mutableIntStateOf(0) }

    var sinConexion by remember { mutableStateOf(!hayInternet(context)) }
    var huboDesconexion by remember { mutableStateOf(false) }
    var mensajeConexion by remember { mutableStateOf("") }

    val swipeState = rememberSwipeRefreshState(isRefreshing = false)

    /* ======================================================
       LISTENER REAL DE CONECTIVIDAD
       ====================================================== */
    DisposableEffect(Unit) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val callback = object : ConnectivityManager.NetworkCallback() {

            override fun onLost(network: Network) {
                sinConexion = true
                huboDesconexion = true
            }

            override fun onAvailable(network: Network) {
                sinConexion = false

                // 🔥 Reconexión REAL Firebase
                FirebaseDatabase.getInstance().goOffline()
                FirebaseDatabase.getInstance().goOnline()

                refreshKey++

                if (huboDesconexion) {
                    mensajeConexion = "La conexión se reestableció"
                    huboDesconexion = false
                }
            }
        }

        cm.registerDefaultNetworkCallback(callback)
        onDispose { cm.unregisterNetworkCallback(callback) }
    }

    /* ======================================================
       PULL TO REFRESH
       ====================================================== */
    SwipeRefresh(
        state = swipeState,
        onRefresh = {
            swipeState.isRefreshing = true

            if (hayInternet(context)) {
                scope.launch {
                    FirebaseDatabase.getInstance().goOffline()
                    delay(300)
                    FirebaseDatabase.getInstance().goOnline()
                    refreshKey++
                }
            }

            swipeState.isRefreshing = false
        }
    ) {

        key(refreshKey) {

            val (modoActualRaw, _, _) =
                LeerFirebase("techo/modo", String::class.java)

            val (estadoTechoRaw, _, _) =
                LeerFirebase("techo/estado", String::class.java)

            val modoActual = modoActualRaw ?: "Desconocido"
            val estadoTecho = estadoTechoRaw ?: "Desconocido"

            var mensajeComando by remember { mutableStateOf("") }

            fun actualizarModo(modo: String) {
                escribirFirebase("techo/modo", modo)
            }

            fun actualizarEstado(estado: String) {
                if (estadoTechoRaw == estado) return
                escribirFirebase("techo/estado", estado)
            }

            /* ======================================================
               REENVÍO OFFLINE
               ====================================================== */
            LaunchedEffect(estadoTechoRaw) {
                if (estadoTechoRaw != null) {
                    val prefs = context.sessionDataStore.data.first()
                    val ultimo = prefs[stringPreferencesKey("last_comando")]
                    if (ultimo != null && ultimo != estadoTechoRaw) {
                        actualizarEstado(ultimo)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0C141A))
                    .systemBarsPadding()
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        "Selecciona el modo del techo",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    ModoCard(
                        "Automático",
                        "El techo se abrirá y cerrará según el clima y la hora.",
                        R.drawable.auto,
                        modoActual == "Automático"
                    ) { actualizarModo("Automático") }

                    ModoCard(
                        "Manual",
                        "Controla manualmente el techo.",
                        R.drawable.manual,
                        modoActual == "Manual"
                    ) { actualizarModo("Manual") }

                    Spacer(Modifier.height(24.dp))

                    if (modoActual == "Manual" && estadoTechoRaw != null) {
                        EstadoTechoPanel(
                            estadoTecho,
                            onAbrir = {
                                scope.launch {
                                    context.sessionDataStore.edit {
                                        it[stringPreferencesKey("last_comando")] = "Abierto"
                                    }
                                }
                                actualizarEstado("Abierto")
                                mensajeComando = "Comando enviado"
                            },
                            onCerrar = {
                                scope.launch {
                                    context.sessionDataStore.edit {
                                        it[stringPreferencesKey("last_comando")] = "Cerrado"
                                    }
                                }
                                actualizarEstado("Cerrado")
                                mensajeComando = "Comando enviado"
                            }
                        )
                    }

                    if (sinConexion) {
                        Text(
                            "Sin conexión. se va a sincronizar cuando se tenga red.",
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                    }

                    if (mensajeConexion.isNotEmpty()) {
                        Text(
                            mensajeConexion,
                            color = Color.Green,
                            fontSize = 14.sp
                        )

                        LaunchedEffect(mensajeConexion) {
                            delay(3000)
                            mensajeConexion = ""
                        }
                    }

                    if (mensajeComando.isNotEmpty()) {
                        Text(mensajeComando, color = Color.Green)
                    }
                }
            }
        }
    }
}

/* ======================================================
   COMPONENTES UI (SIN CAMBIOS)
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
                    titulo,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    descripcion,
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
            "Estado del Techo",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        Text(
            estado,
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
            .size(140.dp, 100.dp)
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
            Spacer(Modifier.height(8.dp))
            Text(texto, color = Color.White, fontSize = 16.sp)
        }
    }
}
