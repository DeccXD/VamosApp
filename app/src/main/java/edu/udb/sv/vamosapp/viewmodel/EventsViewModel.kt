package edu.udb.sv.vamosapp.viewmodel

import androidx.lifecycle.ViewModel
import edu.udb.sv.vamosapp.data.EventRepo
import edu.udb.sv.vamosapp.data.Comment
import edu.udb.sv.vamosapp.data.Rating
import edu.udb.sv.vamosapp.model.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EventsViewModel : ViewModel() {

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    private val _ratings = MutableStateFlow<List<Rating>>(emptyList())
    val ratings: StateFlow<List<Rating>> = _ratings.asStateFlow()

    fun loadEvents() {
        _isLoading.value = true
        _error.value = null

        EventRepo.getUpcomingEvents { list, err ->
            _isLoading.value = false
            if (err != null) _error.value = err else _events.value = list
        }
    }

    fun createEvent(
        title: String,
        date: String,
        time: String,
        location: String,
        description: String,
        creatorId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val event = Event(
            title = title.trim(),
            date = date.trim(),
            time = time.trim(),
            location = location.trim(),
            description = description.trim(),
            creatorId = creatorId
        )

        EventRepo.createEvent(event) { ok, err ->
            if (ok) loadEvents()
            onResult(ok, err)
        }
    }

    fun updateEvent(
        updatedEvent: Event,
        currentUserId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (updatedEvent.creatorId != currentUserId) {
            onResult(false, "No autorizado")
            return
        }

        EventRepo.updateEvent(updatedEvent.id, updatedEvent) { ok, err ->
            if (ok) loadEvents()
            onResult(ok, err)
        }
    }

    fun deleteEvent(
        event: Event,
        currentUserId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (event.creatorId != currentUserId) {
            onResult(false, "No autorizado")
            return
        }

        EventRepo.deleteEvent(event.id) { ok, err ->
            if (ok) loadEvents()
            onResult(ok, err)
        }
    }

    fun toggleAttend(
        event: Event,
        userId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val newState = !event.attendees.contains(userId)

        EventRepo.toggleAttend(event.id, userId, newState) { ok, err ->
            if (ok) loadEvents()
            onResult(ok, err)
        }
    }

    fun addComment(
        eventId: String,
        userId: String,
        userName: String,
        text: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (text.isBlank()) {
            onResult(false, "Comentario vacío")
            return
        }

        EventRepo.addComment(eventId, userId, userName, text) { ok, err ->
            if (ok) loadComments(eventId)
            onResult(ok, err)
        }
    }

    fun setRating(
        eventId: String,
        userId: String,
        userName: String,
        stars: Int,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (stars !in 1..5) {
            onResult(false, "Calificación inválida")
            return
        }

        EventRepo.setRating(eventId, userId, userName, stars) { ok, err ->
            if (ok) {
                loadRatings(eventId)
                loadEvents()
            }
            onResult(ok, err)
        }
    }

    fun loadComments(eventId: String) {
        EventRepo.getComments(eventId) { list, _ ->
            _comments.value = list
        }
    }

    fun loadRatings(eventId: String) {
        EventRepo.getRatings(eventId) { list, _ ->
            _ratings.value = list
        }
    }
}
