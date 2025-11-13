package com.example.firebase_testapp

import BarraNavegacion
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.componentestest.Componentes.Firebase.PantallaLogin
import registrarAdmin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registrarAdmin()
        setContent {
            AppPrincipal()
        }
    }
}

@Composable
fun AppPrincipal() {
    val navController = rememberNavController()
    var isLoggedIn by remember { mutableStateOf(false) }

    if (!isLoggedIn) {
        PantallaLogin(
            onLoginSuccess = { isLoggedIn = true }
        )
    } else {
        Scaffold(
            bottomBar = {
                BarraNavegacion(navController)
            },
            containerColor = Color(0xFF0C141A)
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "clima",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("clima") { PantallaClima(apiKey = "ed3fa63334566ff59bce9d37f4591da9") }
                composable("techo") { ModoTecho() }
                composable("silencio") { ModoSilencio() }
            }
        }
    }
}



