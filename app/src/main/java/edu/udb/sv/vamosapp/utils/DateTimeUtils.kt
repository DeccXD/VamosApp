package edu.udb.sv.vamosapp.utils

import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtils {

    fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun formatTime(hour: Int, minute: Int): String {
        return "%02d:%02d".format(hour, minute)
    }

    fun parseDate(dateString: String): Date? {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.isLenient = false
            sdf.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    fun isPastEvent(dateString: String): Boolean {
        val eventDate = parseDate(dateString) ?: return false

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        return eventDate.before(today)
    }

    fun isFutureEvent(dateString: String): Boolean {
        val eventDate = parseDate(dateString) ?: return false

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        return eventDate.after(today) || eventDate == today
    }
}
