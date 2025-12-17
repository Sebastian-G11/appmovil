package com.example.firebase_testapp

import android.app.TimePickerDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.accompanist.swiperefresh.*
import com.google.firebase.database.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun ProgramacionHoraria() {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var refreshKey by remember { mutableIntStateOf(0) }

    var sinConexion by remember { mutableStateOf(!hayInternet(context)) }
    var huboDesconexion by remember { mutableStateOf(false) }
    var mensajeConexion by remember { mutableStateOf("") }

    val swipeState = rememberSwipeRefreshState(isRefreshing = false)

    val dbRef = remember {
        FirebaseDatabase.getInstance().getReference("programacion_horaria")
    }

    // Variables de programación horaria
    var desde by remember { mutableStateOf("--:--") }
    var hasta by remember { mutableStateOf("--:--") }
    var estado by remember { mutableStateOf("deshabilitado") }

    // Estado del techo
    var estadoTecho by remember { mutableStateOf("Cerrado") }

    val KEY_DESDE = stringPreferencesKey("prog_desde")
    val KEY_HASTA = stringPreferencesKey("prog_hasta")
    val KEY_ESTADO = stringPreferencesKey("prog_estado")
    val KEY_ESTADO_TECHO = stringPreferencesKey("estado_techo")

    /* ======================================================
       LISTENER REAL DE RED
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

            /* --------------------------------------------------
               1) Carga LOCAL inmediata
               -------------------------------------------------- */
            LaunchedEffect(Unit) {
                val prefs = context.sessionDataStore.data.first()
                desde = prefs[KEY_DESDE] ?: "--:--"
                hasta = prefs[KEY_HASTA] ?: "--:--"
                estado = prefs[KEY_ESTADO] ?: "deshabilitado"
                estadoTecho = prefs[KEY_ESTADO_TECHO] ?: "Cerrado"
            }

            /* --------------------------------------------------
               2) Lectura desde Firebase
               -------------------------------------------------- */
            LaunchedEffect(refreshKey) {
                dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        desde = snapshot.child("desde").getValue(String::class.java) ?: desde
                        hasta = snapshot.child("hasta").getValue(String::class.java) ?: hasta
                        estado = snapshot.child("estado").getValue(String::class.java) ?: estado

                        // Sincroniza estado del techo con Firebase si es necesario
                        FirebaseDatabase.getInstance().getReference("techo/estado")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snap: DataSnapshot) {
                                    val estadoFB = snap.getValue(String::class.java)
                                    if (estadoFB != null) estadoTecho = estadoFB
                                }
                                override fun onCancelled(error: DatabaseError) {}
                            })

                        scope.launch {
                            context.sessionDataStore.edit {
                                it[KEY_DESDE] = desde
                                it[KEY_HASTA] = hasta
                                it[KEY_ESTADO] = estado
                                it[KEY_ESTADO_TECHO] = estadoTecho
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }

            /* --------------------------------------------------
               Funciones
               -------------------------------------------------- */
            fun actualizarFirebaseYLocal() {
                dbRef.updateChildren(
                    mapOf(
                        "desde" to desde,
                        "hasta" to hasta,
                        "estado" to estado
                    )
                )

                // También actualiza estado del techo
                FirebaseDatabase.getInstance().getReference("techo/estado")
                    .setValue(estadoTecho)

                scope.launch {
                    context.sessionDataStore.edit {
                        it[KEY_DESDE] = desde
                        it[KEY_HASTA] = hasta
                        it[KEY_ESTADO] = estado
                        it[KEY_ESTADO_TECHO] = estadoTecho
                    }
                }
            }

            fun seleccionarHora(inicio: Boolean) {
                if (estado == "deshabilitado") return

                val c = Calendar.getInstance()
                TimePickerDialog(
                    context,
                    { _, h, m ->
                        val hora = "%02d:%02d".format(h, m)
                        if (inicio) desde = hora else hasta = hora
                        actualizarFirebaseYLocal()
                    },
                    c.get(Calendar.HOUR_OF_DAY),
                    c.get(Calendar.MINUTE),
                    true
                ).show()
            }

            /* --------------------------------------------------
               UI
               -------------------------------------------------- */
            Column(
                modifier = Modifier
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

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2A33)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
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
                                actualizarFirebaseYLocal()
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
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(titulo, color = textoColor, fontSize = 16.sp)
                Text(desc, color = textoColor.copy(alpha = 0.6f), fontSize = 13.sp)
            }

            Text(
                hora,
                color = horaColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
