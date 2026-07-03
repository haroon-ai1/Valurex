package com.nobody.valurex.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ValurexColorScheme = darkColorScheme(
    primary            = ValurexColors.Accent,
    onPrimary          = ValurexColors.TextHigh,
    secondary          = ValurexColors.Accent,
    onSecondary        = ValurexColors.TextHigh,
    background         = ValurexColors.Background,
    onBackground       = ValurexColors.TextHigh,
    surface            = ValurexColors.Surface,
    onSurface          = ValurexColors.TextHigh,
    surfaceVariant     = ValurexColors.SurfaceElevated,
    onSurfaceVariant   = ValurexColors.TextMedium,
    outline            = ValurexColors.Hairline,
    outlineVariant     = ValurexColors.HairlineSubtle,
    error              = ValurexColors.ExpenseRed,
    onError            = ValurexColors.TextHigh,
)

@Composable
fun ValurexTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ValurexColorScheme,
        typography  = ValurexM3Typography,
        shapes      = ValurexShapes,
        content     = content
    )
}
