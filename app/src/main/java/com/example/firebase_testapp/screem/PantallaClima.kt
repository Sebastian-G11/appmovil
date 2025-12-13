package com.example.firebase_testapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.core.content.ContextCompat
import com.example.firebase_testapp.model.*
import com.example.firebase_testapp.network.RetrofitClient
import com.example.firebase_testapp.network.WeatherApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@Composable
fun PantallaClima(apiKey: String) {

    val context = LocalContext.current

    val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    // Estado de permiso de ubicación
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
    }

    var currentWeather by remember { mutableStateOf<Respuesta?>(null) }
    var forecast by remember { mutableStateOf<ForecastResponse?>(null) }

    // Cambio: isLoading inicia en false para permitir reintentos manuales
    var isLoading by remember { mutableStateOf(false) }

    // Mensaje de error para controlar UI de reintento
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    // Solicita permiso al iniciar si no existe
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Cambio: lógica de carga encapsulada para reutilizar en "Reintentar"
    fun cargarClima() {

        if (!hasLocationPermission) {
            errorMessage = "Permiso de ubicación requerido."
            return
        }

        isLoading = true
        errorMessage = null

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->

                if (location == null) {
                    errorMessage = "No se pudo obtener la ubicación."
                    isLoading = false
                    return@addOnSuccessListener
                }

                scope.launch {
                    try {
                        val api =
                            RetrofitClient.instance.create(WeatherApi::class.java)

                        currentWeather = api.getCurrentWeather(
                            location.latitude,
                            location.longitude,
                            apiKey
                        )

                        forecast = api.getForecast(
                            location.latitude,
                            location.longitude,
                            apiKey
                        )

                    } catch (e: Exception) {
                        // Cambio: mensaje claro para falta de internet
                        errorMessage = "Sin conexión a internet. Intenta nuevamente."
                    } finally {
                        isLoading = false
                    }
                }
            }
            .addOnFailureListener {
                errorMessage = "Error al acceder a la ubicación."
                isLoading = false
            }
    }

    // Carga inicial cuando el permiso es concedido
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            cargarClima()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C141A))
            .systemBarsPadding(),  // ← CAMBIO CLAVE
        contentAlignment = Alignment.Center
    )
    {
        when {

            isLoading -> {
                CircularProgressIndicator(color = Color(0xFF23A8F2))
            }

            // Cambio: UI de error con botón Reintentar
            errorMessage != null -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = errorMessage!!,
                        color = Color.White,
                        fontSize = 18.sp
                    )

                    Button(
                        onClick = { cargarClima() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF23A8F2)
                        )
                    ) {
                        Text("Reintentar")
                    }
                }
            }

            currentWeather != null -> {
                val data = currentWeather!!
                val iconId = getWeatherIcon(data.weather.firstOrNull()?.icon ?: "")

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 32.dp)
                ) {

                    Image(
                        painter = painterResource(id = iconId),
                        contentDescription = "Clima actual",
                        modifier = Modifier.size(120.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = data.name,
                        fontSize = 24.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = data.weather.first().description
                            .replaceFirstChar { it.uppercase() },
                        fontSize = 18.sp,
                        color = Color.White
                    )

                    Text(
                        text = "${data.main.temp.toInt()}°C",
                        fontSize = 48.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        InfoCard("Viento", "${data.wind.speed} km/h", R.drawable.wind)
                        InfoCard("Humedad", "${data.main.humidity}%", R.drawable.humedad)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    if (forecast != null) {
                        val nextDays = getNext3DaysForecast(forecast!!)

                        Text(
                            text = "Próximos 3 días",
                            fontSize = 22.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Column {
                            nextDays.forEach { item ->
                                DailyForecastRow(
                                    day = item.dt_txt.substring(0, 10),
                                    iconRes = getWeatherIcon(item.weather.first().icon),
                                    temp = "${item.main.temp.toInt()}°C"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ================= FUNCIONES AUXILIARES ================= */

fun getNext3DaysForecast(forecast: ForecastResponse): List<ForecastItem> {
    return forecast.list
        .groupBy { it.dt_txt.substring(0, 10) }
        .entries
        .take(3)
        .map { entry ->
            val items = entry.value
            val mediodia = items.find { it.dt_txt.contains("12:00:00") }
            mediodia ?: items.getOrNull(items.size / 2) ?: items.first()
        }
}

fun getWeatherIcon(iconCode: String): Int {
    return when (iconCode) {
        "01d" -> R.drawable.sun
        "01n" -> R.drawable.night
        "02d" -> R.drawable.fewclouds
        "02n" -> R.drawable.fewcloudsnight
        "03d", "03n" -> R.drawable.cloud
        "04d", "04n" -> R.drawable.cloud
        "09d", "09n" -> R.drawable.weather
        "10d", "10n" -> R.drawable.weather
        "11d", "11n" -> R.drawable.storm
        "13d", "13n" -> R.drawable.snow
        "50d", "50n" -> R.drawable.mist
        else -> R.drawable.weather
    }
}

@Composable
fun InfoCard(label: String, value: String, iconRes: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111B22)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Text(label, color = Color.White, fontSize = 14.sp)
            Text(value, color = Color(0xFF23A8F2), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DailyForecastRow(day: String, iconRes: Int, temp: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(day, color = Color.White, fontSize = 16.sp)
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )
        Text(temp, color = Color.White, fontSize = 16.sp)
    }
}
