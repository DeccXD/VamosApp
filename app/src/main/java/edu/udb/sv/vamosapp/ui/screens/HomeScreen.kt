package edu.udb.sv.vamosapp.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import edu.udb.sv.vamosapp.auth.AuthViewModel
import edu.udb.sv.vamosapp.model.Event
import edu.udb.sv.vamosapp.viewmodel.EventsViewModel
import edu.udb.sv.vamosapp.utils.DateTimeUtils
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext

private val PrimaryPurple = Color(0xFF4500FF)
private val PrimaryPurpleDark = Color(0xFF5A3AA5)
val SoftPurple = Color(0xFFF1F0F0)
private val CardPurple = Color(0xFFEAE3F7)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    eventsViewModel: EventsViewModel
) {
    val events by eventsViewModel.events.collectAsState()
    val isLoading by eventsViewModel.isLoading.collectAsState()
    val error by eventsViewModel.error.collectAsState()
    val context = LocalContext.current

    val currentUserId = authViewModel.getCurrentUserId().orEmpty()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var eventToDelete by remember { mutableStateOf<Event?>(null) }
    var deleteError by remember { mutableStateOf<String?>(null) }
    var deleting by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { eventsViewModel.loadEvents() }

    if (showDeleteDialog && eventToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                if (!deleting) {
                    showDeleteDialog = false
                    eventToDelete = null
                    deleteError = null
                }
            },
            title = { Text("Eliminar evento") },
            text = {
                Column {
                    Text("¿Seguro que deseas eliminar este evento?")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        eventToDelete!!.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    deleteError?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !deleting,
                    onClick = {
                        deleting = true
                        deleteError = null
                        val ev = eventToDelete!!

                        eventsViewModel.deleteEvent(ev, currentUserId) { ok, err ->
                            deleting = false
                            if (ok) {
                                showDeleteDialog = false
                                eventToDelete = null
                            } else {
                                deleteError = err ?: "No se pudo eliminar"
                            }
                        }
                    }
                ) {
                    Text(if (deleting) "Eliminando..." else "Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !deleting,
                    onClick = {
                        showDeleteDialog = false
                        eventToDelete = null
                        deleteError = null
                    }
                ) { Text("Cancelar") }
            }
        )
    }

    val upcomingEvents = events
        .filter { DateTimeUtils.isFutureEvent(it.date) }
        .sortedBy { DateTimeUtils.parseDate(it.date)?.time ?: it.timestamp }

    val pastEvents = events
        .filter { DateTimeUtils.isPastEvent(it.date) }
        .sortedByDescending { DateTimeUtils.parseDate(it.date)?.time ?: it.timestamp }

    Scaffold(
        containerColor = SoftPurple,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Eventos comunitarios",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    TextButton(onClick = {
                        authViewModel.signOut()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }) {
                        Text("Cerrar Sesión", color = PrimaryPurple)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SoftPurple
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("crear_evento") },
                containerColor = PrimaryPurple,
                contentColor = Color.White
            ) {
                Text("+", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            when {
                isLoading -> Text("Cargando eventos...")
                error != null -> Text(
                    text = error ?: "Error al cargar eventos",
                    color = MaterialTheme.colorScheme.error
                )
                events.isEmpty() -> Text("Aún no hay eventos. Crea el primero con el botón +")
                else -> {

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {

                        if (upcomingEvents.isNotEmpty()) {
                            item {
                                Text(
                                    "Próximos eventos",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(6.dp))
                            }

                            items(upcomingEvents) { event ->
                                EventCard(
                                    event = event,
                                    currentUserId = currentUserId,
                                    navController = navController,
                                    eventsViewModel = eventsViewModel,
                                    onDeleteClick = {
                                        eventToDelete = event
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }

                        if (pastEvents.isNotEmpty()) {
                            item {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Eventos pasados",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(6.dp))
                            }

                            items(pastEvents) { event ->
                                EventCard(
                                    event = event,
                                    currentUserId = currentUserId,
                                    navController = navController,
                                    eventsViewModel = eventsViewModel,
                                    onDeleteClick = {
                                        eventToDelete = event
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }

                        if (upcomingEvents.isEmpty() && pastEvents.isEmpty()) {
                            item { Text("No hay eventos válidos aún") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventCard(
    event: Event,
    currentUserId: String,
    navController: NavController,
    eventsViewModel: EventsViewModel,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current
    val isCreator = event.creatorId == currentUserId
    val isAttending = event.attendees.contains(currentUserId)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("detalle_evento/${event.id}") },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardPurple),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
        ) {
            Text(
                event.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(2.dp))
            Text("${event.date} • ${event.time}", style = MaterialTheme.typography.bodyMedium)
            Text(event.location, style = MaterialTheme.typography.bodyMedium)

            if (event.description.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(event.description, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
       // Asistir - cancelar
                Button(
                    onClick = {
                        eventsViewModel.toggleAttend(event, currentUserId) { _, _ -> }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryPurple,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        if (isAttending) "Cancelar" else "Asistir",
                        fontSize = 16.sp,
                        maxLines = 1
                    )
                }

         //Ver mas
                OutlinedButton(
                    onClick = { navController.navigate("detalle_evento/${event.id}") },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryPurple
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(PrimaryPurple)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ver más", fontSize = 16.sp, maxLines = 1)
                }

//Compartir
                OutlinedButton(
                    onClick = {
                        val shareText = """
                            ${event.title}
                            Fecha: ${event.date} • ${event.time}
                            Lugar: ${event.location}

                            ${event.description}

                            Evento compartido desde VamosApp
                        """.trimIndent()

                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        val shareIntent =
                            Intent.createChooser(sendIntent, "Compartir evento")
                        context.startActivity(shareIntent)
                    },
                    modifier = Modifier
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryPurple
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(PrimaryPurple)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Compartir",
                        tint = PrimaryPurple
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Text("Asistentes: ${event.attendees.size}", style = MaterialTheme.typography.bodySmall)

            if (event.ratingsCount > 0) {
                Text(
                    "Rating: ${"%.1f".format(event.avgRating)} (${event.ratingsCount})",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Text("Sin calificaciones aún", style = MaterialTheme.typography.bodySmall)
            }

            if (isCreator) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { navController.navigate("editar_evento/${event.id}") },
                        modifier = Modifier.height(42.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PrimaryPurpleDark
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Editar", fontSize = 15.sp)
                    }

                    OutlinedButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.height(42.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Eliminar", fontSize = 15.sp)
                    }
                }
            }
        }
    }
}
