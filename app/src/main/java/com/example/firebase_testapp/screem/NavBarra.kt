import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.firebase_testapp.R

@Composable
fun BarraNavegacion(navController: NavHostController) {

    val items = listOf(
        BottomNavItem("Techo", "techo", R.drawable.casa),
        BottomNavItem("Clima", "clima", R.drawable.clima),
        BottomNavItem("Programación", "silencio", R.drawable.reloj)
    )

    val config = LocalConfiguration.current
    val screenHeight = config.screenHeightDp

    // 📏 ALTURA ADAPTATIVA
    val barHeight = when {
        screenHeight < 600 -> 56.dp
        screenHeight < 800 -> 64.dp
        else -> 72.dp
    }

    // 📏 ICONOS ADAPTATIVOS
    val iconSize = when {
        screenHeight < 600 -> 22.dp
        screenHeight < 800 -> 26.dp
        else -> 30.dp
    }

    // 📏 TEXTO ADAPTATIVO
    val textSize = when {
        screenHeight < 600 -> 10.sp
        screenHeight < 800 -> 12.sp
        else -> 14.sp
    }

    NavigationBar(
        containerColor = Color(0xFF111B22),
        modifier = Modifier.height(barHeight)
    ) {
        val currentRoute = currentRoute(navController)

        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Image(
                        painter = painterResource(item.icon),
                        contentDescription = item.label,
                        modifier = Modifier.size(iconSize)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = textSize,
                        color = Color.White
                    )
                },
                alwaysShowLabel = true
            )
        }
    }
}

/* ---------- MODELOS Y HELPERS ---------- */

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
