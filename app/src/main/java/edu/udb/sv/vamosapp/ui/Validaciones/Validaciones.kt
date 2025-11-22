package edu.udb.sv.vamosapp.ui.Validaciones

data class ValidationResult(
    val isValid: Boolean,
    val errors: Map<String, String>
)

object Validaciones {

    fun validarEvento(
        title: String,
        date: String,
        time: String,
        location: String,
        description: String
    ): ValidationResult {

        val errors = mutableMapOf<String, String>()

       //Validaciones
        if (title.isBlank()) errors["title"] = "El título es obligatorio"
        if (location.isBlank()) errors["location"] = "La ubicación es obligatoria"
        if (description.isBlank()) errors["description"] = "La descripción es obligatoria"

        val dateTrim = date.trim()
        val dateRegex = Regex("""\d{2}/\d{2}/\d{4}""") // dd/MM/yyyy

        if (dateTrim.isBlank()) {
            errors["date"] = "La fecha es obligatoria"
        } else if (!dateRegex.matches(dateTrim)) {
            errors["date"] = "Formato inválido. Usa dd/MM/yyyy"
        } else {
            val parts = dateTrim.split("/")
            val day = parts[0].toInt()
            val month = parts[1].toInt()
            val year = parts[2].toInt()

            if (year < 2025) {
                errors["date"] = "El año debe ser 2025 o mayor"
            } else if (month !in 1..12) {
                errors["date"] = "Mes inválido"
            } else {
                val maxDays = daysInMonth(month, year)
                if (day !in 1..maxDays) {
                    errors["date"] = "Día inválido para ese mes"
                }
            }
        }

        val timeTrim = time.trim()
        val timeRegex = Regex("""\d{2}:\d{2}""") // HH:mm

        if (timeTrim.isBlank()) {
            errors["time"] = "La hora es obligatoria"
        } else if (!timeRegex.matches(timeTrim)) {
            errors["time"] = "Formato inválido. Usa HH:mm"
        } else {
            val parts = timeTrim.split(":")
            val hh = parts[0].toInt()
            val mm = parts[1].toInt()

            if (hh !in 0..23) {
                errors["time"] = "Hora inválida (00–23)"
            } else if (mm !in 0..59) {
                errors["time"] = "Minutos inválidos (00–59)"
            }
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }

    private fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    private fun daysInMonth(month: Int, year: Int): Int {
        return when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (isLeapYear(year)) 29 else 28
            else -> 0
        }
    }
}
