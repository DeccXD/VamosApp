package edu.udb.sv.vamosapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import edu.udb.sv.vamosapp.R
import edu.udb.sv.vamosapp.auth.AuthViewModel
import kotlinx.coroutines.delay

enum class SplashMode { STARTUP, AFTER_LOGIN }

@Composable
fun SplashScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    mode: SplashMode
) {
    LaunchedEffect(Unit) {
        delay(2000)
        when (mode) {
            SplashMode.STARTUP -> {
                val isLoggedIn = authViewModel.getCurrentUserId() != null
                if (isLoggedIn) {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                } else {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            }
            SplashMode.AFTER_LOGIN -> {
                navController.navigate("home") {
                    popUpTo("splash_after_login") { inclusive = true }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.vamos_logo),
                contentDescription = "Logo VamosApp",
                modifier = Modifier.size(160.dp)
            )

            Text(
                text = "VamosApp",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Conectando tu comunidad...",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator()
        }
    }
}
