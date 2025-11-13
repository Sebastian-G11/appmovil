package com.example.firebase_testapp.model

data class Respuesta(
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

data class Weather(
    val description: String,
    val icon: String
)

data class Wind(
    val speed: Double
)

data class ForecastItem(
    val dt_txt: String,
    val main: Main,
    val weather: List<Weather>
)
