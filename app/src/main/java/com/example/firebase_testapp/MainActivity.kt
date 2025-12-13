package com.example.firebase_testapp

import BarraNavegacion
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.*
import com.example.componentestest.Componentes.Firebase.PantallaLogin
import com.example.firebase_testapp.loginLogic.SessionManager
import com.example.firebase_testapp.loginLogic.registrarAdmin

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
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    var isLoggedIn by remember { mutableStateOf<Boolean?>(null) }

    // Verifica sesión guardada
    LaunchedEffect(Unit) {
        isLoggedIn = sessionManager.isLoggedIn()
    }

    when (isLoggedIn) {
        null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF23A8F2))
            }
        }

        false -> {
            PantallaLogin(
                onLoginSuccess = {
                    isLoggedIn = true
                }
            )
        }

        true -> {
            val navController = rememberNavController()

            Scaffold(
                containerColor = Color(0xFF0C141A),
                bottomBar = {
                    BarraNavegacion(navController)
                },
                modifier = Modifier.systemBarsPadding()  // Respetar barras del sistema
            ) { innerPadding ->
                // Aplicar padding para asegurar que no se solape con la barra de navegación
                NavHost(
                    navController = navController,
                    startDestination = "clima",
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable("clima") {
                        PantallaClima(apiKey = "ed3fa63334566ff59bce9d37f4591da9")
                    }
                    composable("techo") { ModoTecho() }
                    composable("silencio") { ProgramacionHoraria() }
                }
            }
        }
    }
}
