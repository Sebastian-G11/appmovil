package com.example.firebase_testapp

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp


@Composable
fun EjemploLectura(){
    var sensor by rememberSaveable  { mutableStateOf<SensorData?>(null) }
    var errorMsg by rememberSaveable { mutableStateOf<String?>(null) }
    var loading by rememberSaveable { mutableStateOf(false) }

    LeerFirebase("sensor_data", SensorData::class.java).apply {
        sensor = first ?: sensor
        loading = second
        errorMsg = third



    }

    Text(
        text = "Sensor: ${sensor?.raw_value?:0}",
        modifier = Modifier.padding(16.dp)
    )
}

