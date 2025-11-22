package edu.udb.sv.vamosapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import edu.udb.sv.vamosapp.auth.AuthViewModel
import edu.udb.sv.vamosapp.ui.screens.SplashMode
import edu.udb.sv.vamosapp.ui.screens.SplashScreen
import edu.udb.sv.vamosapp.ui.screens.LoginScreen
import edu.udb.sv.vamosapp.ui.screens.RegisterScreen
import edu.udb.sv.vamosapp.ui.screens.HomeScreen
import edu.udb.sv.vamosapp.ui.screens.CrearEventoScreen
import edu.udb.sv.vamosapp.ui.screens.EditarEventoScreen
import edu.udb.sv.vamosapp.ui.screens.DetalleEventoScreen
import edu.udb.sv.vamosapp.viewmodel.EventsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val navController = rememberNavController()
            val authViewModel: AuthViewModel = viewModel()
            val eventsViewModel: EventsViewModel = viewModel()

            Surface(color = MaterialTheme.colorScheme.background) {
                NavHost(
                    navController = navController,
                    startDestination = "splash"
                ) {
                    composable("splash") {
                        SplashScreen(navController, authViewModel, SplashMode.STARTUP)
                    }

                    composable("splash_after_login") {
                        SplashScreen(navController, authViewModel, SplashMode.AFTER_LOGIN)
                    }

                    composable("login") {
                        LoginScreen(navController, authViewModel)
                    }

                    composable("register") {
                        RegisterScreen(navController, authViewModel)
                    }

                    composable("home") {
                        HomeScreen(navController, authViewModel, eventsViewModel)
                    }

                    composable("crear_evento") {
                        CrearEventoScreen(navController, authViewModel, eventsViewModel)
                    }

                    composable("editar_evento/{eventId}") { backStackEntry ->
                        val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                        EditarEventoScreen(navController, authViewModel, eventId, eventsViewModel)
                    }

                    composable("detalle_evento/{eventId}") { backStackEntry ->
                        val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                        DetalleEventoScreen(navController, authViewModel, eventsViewModel, eventId)
                    }
                }
            }
        }
    }
}
