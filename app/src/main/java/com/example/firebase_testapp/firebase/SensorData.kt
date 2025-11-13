package com.example.firebase_testapp

data class SensorData(
    var device: String = "",
    var raw_value: Int = 0,
    var timestamp: Int = 0,
    var voltage: Double = 0.0,
    var wifi_rssi: Int = 0
) {
    // Constructor vacío OBLIGATORIO para Firebase
    constructor() : this("", 0, 0, 0.0, 0)
}