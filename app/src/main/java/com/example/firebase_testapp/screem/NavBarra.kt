import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.firebase_testapp.R

@Composable
fun BarraNavegacion(navController: NavHostController) {
    val items = listOf(
        BottomNavItem("Techo", "techo", R.drawable.casa),
        BottomNavItem("Clima", "clima", R.drawable.clima),
        BottomNavItem("Silencio", "silencio", R.drawable.campana)
    )

    NavigationBar(
        containerColor = Color(0xFF111B22),
        modifier = Modifier.height(60.dp)
    ) {
        val currentRoute = currentRoute(navController)
        items.forEach { item ->
            NavigationBarItem(
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
                }
            )
        }
    }
}

// ✅ Eliminamos el "TODO" duplicado
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
