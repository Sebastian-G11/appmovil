package com.example.firebase_testapp

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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

@Composable
fun BarraNavegacion(navController: NavHostController) {
    val items = listOf(
        BottomNavItem("Techo", "techo", R.drawable.casa),
        BottomNavItem("Clima", "clima", R.drawable.clima),
        BottomNavItem("Silencio", "silencio", R.drawable.campana)
    )

    NavigationBar(
        containerColor = Color(0xFF111B22,),
        modifier = Modifier.height(60.dp)
    ) {

        val currentRoute = currentRoute(navController)
        items.forEach { item ->
            NavigationBarItem(
                modifier = Modifier.size(25.dp),
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true

                    }
                },
                icon = {
                    Image(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.label,
                        modifier = Modifier.size(25.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        color = if (currentRoute == item.route) Color.White else Color.White,
                        fontSize = MaterialTheme.typography.labelSmall.fontSize
                    )
                },
            )
        }
    }
}

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: Int
)

@Composable
fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}
