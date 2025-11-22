package edu.udb.sv.vamosapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import edu.udb.sv.vamosapp.auth.AuthViewModel
import edu.udb.sv.vamosapp.viewmodel.EventsViewModel
import edu.udb.sv.vamosapp.ui.Validaciones.Validaciones

private fun formatDateInput(raw: String): String {
    val digits = raw.take(8)
    return buildString {
        for (i in digits.indices) {
            append(digits[i])
            if (i == 1 || i == 3) append('/')
        }
    }
}

private fun formatTimeInput(raw: String): String {
    val digits = raw.take(4)
    return buildString {
        for (i in digits.indices) {
            append(digits[i])
            if (i == 1) append(':')
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearEventoScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    eventsViewModel: EventsViewModel
) {
    var titulo by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    var guardando by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var fieldErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    val creadorId = authViewModel.getCurrentUserId().orEmpty()

    fun validarForm(): Boolean {
        val result = Validaciones.validarEvento(
            title = titulo,
            date = fecha,
            time = hora,
            location = ubicacion,
            description = descripcion
        )
        fieldErrors = result.errors
        return result.isValid
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Crear evento") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 18.dp, vertical = 14.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Nuevo evento",
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedTextField(
                value = titulo,
                onValueChange = {
                    titulo = it
                    if (fieldErrors.isNotEmpty()) validarForm()
                },
                label = { Text("Título") },
                shape = MaterialTheme.shapes.medium,
                isError = fieldErrors.containsKey("title"),
                supportingText = {
                    fieldErrors["title"]?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = fecha,
                onValueChange = { input ->
                    val digits = input.filter { it.isDigit() }
                    fecha = formatDateInput(digits)
                    if (fieldErrors.isNotEmpty()) validarForm()
                },
                label = { Text("Fecha (dd/MM/yyyy)") },
                placeholder = { Text("20/11/2025") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                isError = fieldErrors.containsKey("date"),
                supportingText = {
                    fieldErrors["date"]?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = hora,
                onValueChange = { input ->
                    val digits = input.filter { it.isDigit() }
                    hora = formatTimeInput(digits)
                    if (fieldErrors.isNotEmpty()) validarForm()
                },
                label = { Text("Hora (HH:mm)") },
                placeholder = { Text("18:00") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                isError = fieldErrors.containsKey("time"),
                supportingText = {
                    fieldErrors["time"]?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = ubicacion,
                onValueChange = {
                    ubicacion = it
                    if (fieldErrors.isNotEmpty()) validarForm()
                },
                label = { Text("Ubicación") },
                shape = MaterialTheme.shapes.medium,
                isError = fieldErrors.containsKey("location"),
                supportingText = {
                    fieldErrors["location"]?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = {
                    descripcion = it
                    if (fieldErrors.isNotEmpty()) validarForm()
                },
                label = { Text("Descripción") },
                minLines = 4,
                shape = MaterialTheme.shapes.medium,
                isError = fieldErrors.containsKey("description"),
                supportingText = {
                    fieldErrors["description"]?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMsg != null) {
                Text(text = errorMsg ?: "", color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    errorMsg = null
                    val ok = validarForm()

                    if (!ok) {
                        errorMsg = "Revisa los campos marcados"
                        return@Button
                    }
                    if (creadorId.isBlank()) {
                        errorMsg = "Usuario no autenticado"
                        return@Button
                    }

                    guardando = true

                    eventsViewModel.createEvent(
                        title = titulo,
                        date = fecha,
                        time = hora,
                        location = ubicacion,
                        description = descripcion,
                        creatorId = creadorId
                    ) { success, err ->
                        guardando = false
                        if (success) navController.popBackStack()
                        else errorMsg = err ?: "Error al crear el evento"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = !guardando,
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                )
            ) {
                Text(
                    text = if (guardando) "Guardando..." else "Crear evento",
                    fontSize = 18.sp
                )
            }
        }
    }
}
