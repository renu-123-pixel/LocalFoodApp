package com.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Brand Colors - Updated to a Fresh Green Theme
val OrangeDeep   = Color(0xFF2E7D32) // Deep Green
val OrangeLight  = Color(0xFF66BB6A) // Light Green
val OrangePale   = Color(0xFFE8F5E9) // Pale Green
val GreenVeg     = Color(0xFF4CAF50)
val GreenPale    = Color(0xFFE8F5E9)
val RedNonVeg    = Color(0xFFD32F2F)
val BackgroundCream = Color(0xFFF1F8E9)
val CardWhite    = Color(0xFFFFFFFF)
val TextDark     = Color(0xFF1B5E20)
val TextGray     = Color(0xFF558B2F)
val DividerGray  = Color(0xFFDCEDC8)

private val AppColorScheme = lightColorScheme(
    primary        = OrangeDeep,
    onPrimary      = Color.White,
    secondary      = OrangeLight,
    onSecondary    = Color.White,
    background     = BackgroundCream,
    onBackground   = TextDark,
    surface        = CardWhite,
    onSurface      = TextDark,
    error          = RedNonVeg,
)

@Composable
fun JashpurBhojanTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        content = content
    )
}
