package edu.udb.sv.vamosapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun Stars(
    stars: Int,
    onSelect: (Int) -> Unit
) {
    Row {
        for (i in 1..5) {
            val filled = i <= stars

            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = if (filled) Color(0xFFFFD700) else Color.Gray,
                modifier = Modifier.clickable { onSelect(i) }
            )
        }
    }
}
