package com.example.firebase_testapp.screem
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import com.google.firebase.database.FirebaseDatabase

import android.app.TimePickerDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.datastore.preferences.core.*
import com.example.firebase_testapp.sessionDataStore
import com.google.firebase.database.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.example.firebase_testapp.R
import com.example.firebase_testapp.hayInternet

data class AccionUsuario(
    val accion: String = "",
    val timestamp: Long = 0L,
    val detalle: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAjustes() {
    val KEY_MODO_NOCHE = booleanPreferencesKey("modo_noche")
    val KEY_DESDE_NOCHE = stringPreferencesKey("desde_noche")
    val KEY_HASTA_NOCHE = stringPreferencesKey("hasta_noche")

    val KEY_HUM_MIN = floatPreferencesKey("hum_min")
    val KEY_HUM_MAX = floatPreferencesKey("hum_max")
    val context = LocalContext.current
    var sinConexion by remember { mutableStateOf(!hayInternet(context)) }
    var huboDesconexion by remember { mutableStateOf(false) }


    val scope = rememberCoroutineScope()
    val dbRef = remember { FirebaseDatabase.getInstance().getReference("config_notificaciones") }

    var tabSeleccionada by remember { mutableStateOf(0) }

    // Estados para Modo Noche
    var modoNocheHabilitado by remember { mutableStateOf(false) }
    var desdeNoche by remember { mutableStateOf("22:00") }
    var hastaNoche by remember { mutableStateOf("07:00") }

    // Estados para Rango Humedad
    var humedadMin by remember { mutableStateOf(30f) }
    var humedadMax by remember { mutableStateOf(70f) }

    // Historial de acciones (local)
    var historialAcciones by remember { mutableStateOf<List<AccionUsuario>>(emptyList()) }
    DisposableEffect(Unit) {

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager

        val callback = object : ConnectivityManager.NetworkCallback() {

            override fun onLost(network: Network) {
                sinConexion = true
                huboDesconexion = true
            }

            override fun onAvailable(network: Network) {
                sinConexion = false

                // 🔥 RECONEXIÓN REAL DE FIREBASE
                FirebaseDatabase.getInstance().goOffline()
                FirebaseDatabase.getInstance().goOnline()

                huboDesconexion = false
            }
        }

        cm.registerDefaultNetworkCallback(callback)
        onDispose { cm.unregisterNetworkCallback(callback) }
    }

    // Cargar configuraciones desde Firebase
    LaunchedEffect(Unit) {
        val prefs = context.sessionDataStore.data.first()

        modoNocheHabilitado = prefs[KEY_MODO_NOCHE] ?: false
        desdeNoche = prefs[KEY_DESDE_NOCHE] ?: "22:00"
        hastaNoche = prefs[KEY_HASTA_NOCHE] ?: "07:00"

        humedadMin = prefs[KEY_HUM_MIN] ?: 30f
        humedadMax = prefs[KEY_HUM_MAX] ?: 70f
    }
    LaunchedEffect(Unit) {
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                modoNocheHabilitado =
                    snapshot.child("modo_noche/habilitado")
                        .getValue(String::class.java)
                        ?.toBoolean() ?: modoNocheHabilitado

                desdeNoche =
                    snapshot.child("modo_noche/desde")
                        .getValue(String::class.java) ?: desdeNoche

                hastaNoche =
                    snapshot.child("modo_noche/hasta")
                        .getValue(String::class.java) ?: hastaNoche

                humedadMin =
                    snapshot.child("rango_humedad/min")
                        .getValue(Int::class.java)
                        ?.toFloat() ?: humedadMin

                humedadMax =
                    snapshot.child("rango_humedad/max")
                        .getValue(Int::class.java)
                        ?.toFloat() ?: humedadMax

                // 🔥 sincroniza local
                scope.launch {
                    context.sessionDataStore.edit {
                        it[KEY_MODO_NOCHE] = modoNocheHabilitado
                        it[KEY_DESDE_NOCHE] = desdeNoche
                        it[KEY_HASTA_NOCHE] = hastaNoche
                        it[KEY_HUM_MIN] = humedadMin
                        it[KEY_HUM_MAX] = humedadMax
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


    fun actualizarModoNoche() {
        dbRef.child("modo_noche").setValue(
            mapOf(
                "habilitado" to modoNocheHabilitado.toString(),
                "desde" to desdeNoche,
                "hasta" to hastaNoche
            )
        )

        scope.launch {
            context.sessionDataStore.edit {
                it[KEY_MODO_NOCHE] = modoNocheHabilitado
                it[KEY_DESDE_NOCHE] = desdeNoche
                it[KEY_HASTA_NOCHE] = hastaNoche
            }
        }
    }


    fun actualizarRangoHumedad() {
        dbRef.child("rango_humedad").setValue(
            mapOf(
                "min" to humedadMin.toInt(),
                "max" to humedadMax.toInt()
            )
        )

        scope.launch {
            context.sessionDataStore.edit {
                it[KEY_HUM_MIN] = humedadMin
                it[KEY_HUM_MAX] = humedadMax
            }
        }
    }


    fun seleccionarHoraNoche(esDesde: Boolean) {
        if (!modoNocheHabilitado) return

        val c = Calendar.getInstance()
        TimePickerDialog(
            context,
            { _, h, m ->
                val hora = "%02d:%02d".format(h, m)
                if (esDesde) desdeNoche = hora else hastaNoche = hora
                actualizarModoNoche()
            },
            c.get(Calendar.HOUR_OF_DAY),
            c.get(Calendar.MINUTE),
            true
        ).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C141A))
            .systemBarsPadding()
    ) {
        // Header con tabs
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF111B22))
                .padding(24.dp)
        ) {
            Text(
                text = "Configuración",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            TabRow(
                selectedTabIndex = tabSeleccionada,
                containerColor = Color.Transparent,
                contentColor = Color(0xFF23A8F2)
            ) {
                Tab(
                    selected = tabSeleccionada == 0,
                    onClick = { tabSeleccionada = 0 },
                    text = {
                        Text(
                            "Notificaciones",
                            fontSize = 14.sp,
                            color = if (tabSeleccionada == 0) Color(0xFF23A8F2) else Color(0xFF8FA0AB)
                        )
                    }
                )
                Tab(
                    selected = tabSeleccionada == 1,
                    onClick = { tabSeleccionada = 1 },
                    text = {
                        Text(
                            "Historial",
                            fontSize = 14.sp,
                            color = if (tabSeleccionada == 1) Color(0xFF23A8F2) else Color(0xFF8FA0AB)
                        )
                    }
                )
            }
        }

        // Contenido según tab seleccionada
        when (tabSeleccionada) {
            0 -> {
                // TAB DE NOTIFICACIONES
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // MODO NOCHE
                    Text(
                        "Modo Noche",
                        color = Color.White,
                        fontSize = 18.sp,
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
                                Text("Silenciar notificaciones", color = Color.White, fontSize = 16.sp)
                                Text(
                                    "Desactiva notificaciones en horario nocturno",
                                    color = Color(0xFF8FA0AB),
                                    fontSize = 13.sp
                                )
                            }

                            Switch(
                                checked = modoNocheHabilitado,
                                onCheckedChange = {
                                    modoNocheHabilitado = it
                                    actualizarModoNoche()
                                }
                            )
                        }
                    }

                    ConfigHoraCard(
                        "Desde",
                        "Hora de inicio del silencio",
                        desdeNoche,
                        modoNocheHabilitado
                    ) { seleccionarHoraNoche(true) }

                    ConfigHoraCard(
                        "Hasta",
                        "Hora de fin del silencio",
                        hastaNoche,
                        modoNocheHabilitado
                    ) { seleccionarHoraNoche(false) }

                    Divider(color = Color(0xFF263238), thickness = 1.dp)

                    // RANGO HUMEDAD
                    Text(
                        "Alertas de Humedad",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2A33)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text(
                                "Rango permitido: ${humedadMin.toInt()}% - ${humedadMax.toInt()}%",
                                color = Color(0xFF23A8F2),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text("Humedad Mínima", color = Color.White, fontSize = 14.sp)
                            Slider(
                                value = humedadMin,
                                onValueChange = { humedadMin = it },
                                onValueChangeFinished = { actualizarRangoHumedad() },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF23A8F2),
                                    activeTrackColor = Color(0xFF23A8F2)
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Humedad Máxima", color = Color.White, fontSize = 14.sp)
                            Slider(
                                value = humedadMax,
                                onValueChange = { humedadMax = it },
                                onValueChangeFinished = { actualizarRangoHumedad() },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF23A8F2),
                                    activeTrackColor = Color(0xFF23A8F2)
                                )
                            )
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF12212B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Recibirás notificaciones cuando la humedad esté fuera del rango configurado.",
                            color = Color(0xFF23A8F2),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            1 -> {
                // TAB DE HISTORIAL
                if (historialAcciones.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painter = painterResource(R.drawable.reloj),
                                contentDescription = null,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No hay acciones registradas",
                                color = Color(0xFF8FA0AB),
                                fontSize = 16.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(historialAcciones) { accion ->
                            AccionCard(accion)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConfigHoraCard(
    titulo: String,
    desc: String,
    hora: String,
    habilitado: Boolean,
    onClick: () -> Unit
) {
    val fondo = if (habilitado) Color(0xFF1C2A33) else Color(0xFF2A2A2A)
    val textoColor = if (habilitado) Color.White else Color(0xFF777777)
    val horaColor = if (habilitado) Color(0xFF23A8F2) else Color(0xFF555555)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = fondo),
        modifier = Modifier
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

@Composable
fun AccionCard(accion: AccionUsuario) {
    val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        .format(Date(accion.timestamp))

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2A33)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = accion.accion,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = fecha,
                    color = Color(0xFF8FA0AB),
                    fontSize = 12.sp
                )
            }
            if (accion.detalle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = accion.detalle,
                    color = Color(0xFF23A8F2),
                    fontSize = 13.sp
                )
            }
        }
    }
}

// Helper para guardar acciones localmente
fun guardarAccion(
    context: android.content.Context,
    scope: kotlinx.coroutines.CoroutineScope,
    accion: String,
    detalle: String
) {
    scope.launch {
        context.sessionDataStore.edit { prefs ->
            val historialActual = prefs[stringPreferencesKey("historial_acciones")] ?: "[]"
            val lista = parseHistorial(historialActual).toMutableList()

            lista.add(0, AccionUsuario(accion, System.currentTimeMillis(), detalle))

            // Limitar a últimas 50 acciones
            if (lista.size > 50) {
                lista.removeAt(lista.size - 1)
            }

            prefs[stringPreferencesKey("historial_acciones")] = serializeHistorial(lista)
        }
    }
}

// Helper para parsear JSON simple del historial
fun parseHistorial(json: String): List<AccionUsuario> {
    if (json == "[]") return emptyList()

    val lista = mutableListOf<AccionUsuario>()
    val items = json.trim('[', ']').split("},{")

    items.forEach { item ->
        val limpio = item.trim('{', '}')
        val partes = limpio.split(",")

        var accion = ""
        var timestamp = 0L
        var detalle = ""

        partes.forEach { parte ->
            val kv = parte.split(":")
            if (kv.size == 2) {
                val key = kv[0].trim('"')
                val value = kv[1].trim('"')
                when (key) {
                    "accion" -> accion = value
                    "timestamp" -> timestamp = value.toLongOrNull() ?: 0L
                    "detalle" -> detalle = value
                }
            }
        }

        lista.add(AccionUsuario(accion, timestamp, detalle))
    }

    return lista
}

fun serializeHistorial(lista: List<AccionUsuario>): String {
    if (lista.isEmpty()) return "[]"

    val items = lista.map { accion ->
        """{"accion":"${accion.accion}","timestamp":${accion.timestamp},"detalle":"${accion.detalle}"}"""
    }

    return "[${items.joinToString(",")}]"
}