package edu.udb.sv.vamosapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import edu.udb.sv.vamosapp.auth.AuthViewModel
import edu.udb.sv.vamosapp.ui.Validaciones.Validaciones
import edu.udb.sv.vamosapp.viewmodel.EventsViewModel

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
fun EditarEventoScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    eventId: String,
    eventsViewModel: EventsViewModel
) {
    val events by eventsViewModel.events.collectAsState()
    val event = events.firstOrNull { it.id == eventId }
    val currentUserId = authViewModel.getCurrentUserId().orEmpty()

    LaunchedEffect(Unit) {
        if (events.isEmpty()) eventsViewModel.loadEvents()
    }

    val primaryPurple = Color(0xFF4700FF)
    val softBg = MaterialTheme.colorScheme.background

    if (event == null) {
        Scaffold(
            topBar = { TopAppBar(title = { Text("Editar evento") }) }
        ) { padding ->
            Box(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(softBg),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("Cargando evento...")
            }
        }
        return
    }


    if (event.creatorId != currentUserId) {
        Scaffold(
            topBar = { TopAppBar(title = { Text("Editar evento") }) }
        ) { padding ->
            Column(
                Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
                    .background(softBg),
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "No tienes permiso para editar este evento.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryPurple,
                                contentColor = Color.White
                            ),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Text("Volver", fontSize = 18.sp)
                        }
                    }
                }
            }
        }
        return
    }

    var titulo by remember { mutableStateOf(event.title) }
    var fecha by remember { mutableStateOf(event.date) }
    var hora by remember { mutableStateOf(event.time) }
    var ubicacion by remember { mutableStateOf(event.location) }
    var descripcion by remember { mutableStateOf(event.description) }

    var guardando by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var fieldErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

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
        topBar = {
            TopAppBar(
                title = { Text("Editar evento") }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(softBg)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Text(
                        "Actualiza la información",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = titulo,
                        onValueChange = {
                            titulo = it
                            if (fieldErrors.isNotEmpty()) validarForm()
                        },
                        label = { Text("Título") },
                        isError = fieldErrors.containsKey("title"),
                        supportingText = {
                            fieldErrors["title"]?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )

                    OutlinedTextField(
                        value = fecha,
                        onValueChange = { input ->
                            val onlyDigits = input.filter { it.isDigit() }
                            fecha = formatDateInput(onlyDigits)
                            if (fieldErrors.isNotEmpty()) validarForm()
                        },
                        label = { Text("Fecha (dd/MM/yyyy)") },
                        singleLine = true,
                        isError = fieldErrors.containsKey("date"),
                        supportingText = {
                            fieldErrors["date"]?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )

                    OutlinedTextField(
                        value = hora,
                        onValueChange = { input ->
                            val onlyDigits = input.filter { it.isDigit() }
                            hora = formatTimeInput(onlyDigits)
                            if (fieldErrors.isNotEmpty()) validarForm()
                        },
                        label = { Text("Hora (HH:mm)") },
                        singleLine = true,
                        isError = fieldErrors.containsKey("time"),
                        supportingText = {
                            fieldErrors["time"]?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )

                    OutlinedTextField(
                        value = ubicacion,
                        onValueChange = {
                            ubicacion = it
                            if (fieldErrors.isNotEmpty()) validarForm()
                        },
                        label = { Text("Ubicación") },
                        isError = fieldErrors.containsKey("location"),
                        supportingText = {
                            fieldErrors["location"]?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = {
                            descripcion = it
                            if (fieldErrors.isNotEmpty()) validarForm()
                        },
                        label = { Text("Descripción") },
                        minLines = 3,
                        isError = fieldErrors.containsKey("description"),
                        supportingText = {
                            fieldErrors["description"]?.let {
                                Text(it, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )

                    errorMsg?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }

                    Spacer(Modifier.height(4.dp))

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        enabled = !guardando,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryPurple,
                            contentColor = Color.White
                        ),
                        shape = MaterialTheme.shapes.large,
                        onClick = {
                            errorMsg = null
                            if (!validarForm()) {
                                errorMsg = "Revisa los campos marcados"
                                return@Button
                            }

                            guardando = true
                            val updatedEvent = event.copy(
                                title = titulo,
                                date = fecha,
                                time = hora,
                                location = ubicacion,
                                description = descripcion
                            )

                            eventsViewModel.updateEvent(updatedEvent, currentUserId) { ok, err ->
                                guardando = false
                                if (ok) navController.popBackStack()
                                else errorMsg = err ?: "Error al guardar cambios"
                            }
                        }
                    ) {
                        Text(
                            if (guardando) "Guardando..." else "Guardar cambios",
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}
