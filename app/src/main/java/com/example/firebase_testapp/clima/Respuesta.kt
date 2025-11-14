package com.example.firebase_testapp.model

data class Respuesta(//Recopila datos
    val name: String = "",
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind,
    val list: List<ForecastItem>? = null
)

data class Main(
    val temp: Double,
    val humidity: Int
)

data class Weather(//describe el clima
    val description: String,
    val icon: String
)

data class Wind(//velocidad del viento
    val speed: Double
)

data class ForecastResponse(//lista de los dias
    val list: List<ForecastItem>
)

data class ForecastItem(
    val dt_txt: String,
    val main: Main,
    val weather: List<Weather>
)

