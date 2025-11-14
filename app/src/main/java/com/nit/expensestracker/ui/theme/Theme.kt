package com.nit.expensestracker

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ExpensesTrackerTheme(content: @Composable () -> Unit) {
    val colorScheme = lightColorScheme(
        primary = Color(0xFF4CAF50),
        onPrimary = Color.White,
        secondary = Color(0xFF81C784),
        tertiary = Color(0xFFFFB74D),
        background = Color(0xFFF7F9F8),
        surface = Color.White,
        onBackground = Color.Black,
        onSurface = Color(0xFF1C1B1F)
    )

    val typography = Typography(
        titleLarge = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        ),
        bodyMedium = TextStyle(
            fontSize = 16.sp
        ),
        labelMedium = TextStyle(
            fontSize = 14.sp,
            color = Color.DarkGray
        )
    )

    val shapes = Shapes(
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(24.dp)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = shapes,
        content = content
    )
}
