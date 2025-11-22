package edu.udb.sv.vamosapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import edu.udb.sv.vamosapp.auth.AuthViewModel
import edu.udb.sv.vamosapp.viewmodel.EventsViewModel
import edu.udb.sv.vamosapp.ui.components.Stars

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleEventoScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    eventsViewModel: EventsViewModel,
    eventId: String
) {
    val events by eventsViewModel.events.collectAsState()
    val comments by eventsViewModel.comments.collectAsState()
    val ratings by eventsViewModel.ratings.collectAsState()

    val currentUserId = authViewModel.getCurrentUserId().orEmpty()
    val currentUserName = authViewModel.getCurrentUserName()

    var commentText by remember { mutableStateOf("") }
    var selectedStars by remember { mutableStateOf(0) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var sending by remember { mutableStateOf(false) }

    val event = events.firstOrNull { it.id == eventId }

    LaunchedEffect(eventId) {
        if (events.isEmpty()) eventsViewModel.loadEvents()
        eventsViewModel.loadComments(eventId)
        eventsViewModel.loadRatings(eventId)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Detalle del evento",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Text(
                            "<",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->

        if (event == null) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Cargando evento...")
            }
            return@Scaffold
        }

        val isAttending = event.attendees.contains(currentUserId)
        val ratingMap = ratings.associateBy({ it.userId }, { it.stars })

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(event.title, style = MaterialTheme.typography.titleLarge)
                    Text("${event.date} • ${event.time}")
                    Text(event.location)

                    if (event.description.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(event.description)
                    }

                    Spacer(Modifier.height(8.dp))
                    Text("Asistentes: ${event.attendees.size}")

                    if (event.ratingsCount > 0) {
                        Text("Rating: ${"%.1f".format(event.avgRating)} (${event.ratingsCount})")
                    } else {
                        Text("Sin calificaciones aún")
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                "Comentarios",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(comments) { c ->
                    val stars = ratingMap[c.userId] ?: 0
                    val nameToShow = c.userName.ifBlank { c.userId }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    nameToShow,
                                    style = MaterialTheme.typography.titleSmall
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (stars > 0) {
                                        Text("$stars/5")
                                        Spacer(Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFFFD700)
                                        )
                                    } else {
                                        Text("Sin rating")
                                    }
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(c.text)
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            if (isAttending) {

                Text("Tu calificación", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Stars(stars = selectedStars, onSelect = { selectedStars = it })

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    label = { Text("Escribe un comentario") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                )

                errorMsg?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .height(50.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    enabled = !sending,
                    onClick = {
                        errorMsg = null

                        if (currentUserId.isBlank()) {
                            errorMsg = "Debes iniciar sesión"
                            return@Button
                        }

                        if (selectedStars == 0) {
                            errorMsg = "Selecciona estrellas"
                            return@Button
                        }

                        if (commentText.isBlank()) {
                            errorMsg = "Escribe un comentario"
                            return@Button
                        }

                        sending = true

                        eventsViewModel.setRating(
                            eventId = eventId,
                            userId = currentUserId,
                            userName = currentUserName,
                            stars = selectedStars
                        ) { okRating, errRating ->
                            if (!okRating) {
                                sending = false
                                errorMsg = errRating ?: "No se pudo calificar"
                                return@setRating
                            }

                            eventsViewModel.addComment(
                                eventId = eventId,
                                userId = currentUserId,
                                userName = currentUserName,
                                text = commentText
                            ) { okComment, errComment ->
                                sending = false
                                if (okComment) {
                                    commentText = ""
                                    selectedStars = 0
                                } else {
                                    errorMsg = errComment ?: "No se pudo comentar"
                                }
                            }
                        }
                    }
                ) {
                    Text(
                        if (sending) "Enviando..." else "Enviar comentario y calificación",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

            } else {
                Text(
                    "Debes asistir para comentar o calificar",
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}
