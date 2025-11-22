package edu.udb.sv.vamosapp.model

data class Event(
    val id: String = "",
    val title: String = "",
    val date: String = "",
    val time: String = "",
    val location: String = "",
    val description: String = "",
    val creatorId: String = "",
    val timestamp: Long = 0L,
    val attendees: List<String> = emptyList(),
    val avgRating: Double = 0.0,
    val ratingsCount: Int = 0
)
