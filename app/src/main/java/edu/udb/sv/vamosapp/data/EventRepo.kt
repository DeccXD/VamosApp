package edu.udb.sv.vamosapp.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import edu.udb.sv.vamosapp.model.Event

data class Comment(
    val userId: String = "",
    val userName: String = "",
    val text: String = "",
    val timestamp: Long = 0L
)

data class Rating(
    val userId: String = "",
    val userName: String = "",
    val stars: Int = 0,
    val timestamp: Long = 0L
)

object EventRepo {

    private val db = FirebaseFirestore.getInstance()
    private val eventsCollection = db.collection("events")

    fun createEvent(
        event: Event,
        onResult: (Boolean, String?) -> Unit
    ) {
        val docRef = eventsCollection.document()

        val data = event.copy(
            id = docRef.id,
            timestamp = System.currentTimeMillis(),
            attendees = emptyList(),
            avgRating = 0.0,
            ratingsCount = 0
        )

        docRef.set(data)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.message) }
    }

    fun getUpcomingEvents(
        onResult: (List<Event>, String?) -> Unit
    ) {
        eventsCollection
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.map { doc ->
                    Event(
                        id = doc.getString("id") ?: doc.id,
                        title = doc.getString("title") ?: "",
                        date = doc.getString("date") ?: "",
                        time = doc.getString("time") ?: "",
                        location = doc.getString("location") ?: "",
                        description = doc.getString("description") ?: "",
                        creatorId = doc.getString("creatorId") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        attendees = doc.get("attendees") as? List<String> ?: emptyList(),
                        avgRating = doc.getDouble("avgRating") ?: 0.0,
                        ratingsCount = (doc.getLong("ratingsCount") ?: 0L).toInt()
                    )
                }
                onResult(list, null)
            }
            .addOnFailureListener { e ->
                onResult(emptyList(), e.message)
            }
    }

    fun updateEvent(
        eventId: String,
        updatedEvent: Event,
        onResult: (Boolean, String?) -> Unit
    ) {
        val data = mapOf(
            "title" to updatedEvent.title,
            "date" to updatedEvent.date,
            "time" to updatedEvent.time,
            "location" to updatedEvent.location,
            "description" to updatedEvent.description
        )

        eventsCollection.document(eventId)
            .update(data)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.message) }
    }

    fun deleteEvent(
        eventId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        eventsCollection.document(eventId)
            .delete()
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.message) }
    }

    fun toggleAttend(
        eventId: String,
        userId: String,
        attending: Boolean,
        onResult: (Boolean, String?) -> Unit
    ) {
        val update = if (attending) {
            mapOf("attendees" to FieldValue.arrayUnion(userId))
        } else {
            mapOf("attendees" to FieldValue.arrayRemove(userId))
        }

        eventsCollection.document(eventId)
            .update(update)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.message) }
    }

    fun addComment(
        eventId: String,
        userId: String,
        userName: String,
        text: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val commentData = mapOf(
            "userId" to userId,
            "userName" to userName.trim(),
            "text" to text.trim(),
            "timestamp" to System.currentTimeMillis()
        )

        eventsCollection.document(eventId)
            .collection("comments")
            .add(commentData)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.message) }
    }

    fun getComments(
        eventId: String,
        onResult: (List<Comment>, String?) -> Unit
    ) {
        eventsCollection.document(eventId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.map { d ->
                    Comment(
                        userId = d.getString("userId") ?: "",
                        userName = d.getString("userName") ?: "",
                        text = d.getString("text") ?: "",
                        timestamp = d.getLong("timestamp") ?: 0L
                    )
                }
                onResult(list, null)
            }
            .addOnFailureListener { e ->
                onResult(emptyList(), e.message)
            }
    }

    fun setRating(
        eventId: String,
        userId: String,
        userName: String,
        stars: Int,
        onResult: (Boolean, String?) -> Unit
    ) {
        val ratingData = mapOf(
            "userId" to userId,
            "userName" to userName.trim(),
            "stars" to stars,
            "timestamp" to System.currentTimeMillis()
        )

        eventsCollection.document(eventId)
            .collection("ratings")
            .document(userId)
            .set(ratingData)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.message) }
    }

    fun getRatings(
        eventId: String,
        onResult: (List<Rating>, String?) -> Unit
    ) {
        eventsCollection.document(eventId)
            .collection("ratings")
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.map { d ->
                    Rating(
                        userId = d.getString("userId") ?: d.id,
                        userName = d.getString("userName") ?: "",
                        stars = (d.getLong("stars") ?: 0L).toInt(),
                        timestamp = d.getLong("timestamp") ?: 0L
                    )
                }
                onResult(list, null)
            }
            .addOnFailureListener { e ->
                onResult(emptyList(), e.message)
            }
    }
}
