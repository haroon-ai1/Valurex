package com.nobody.valurex.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nobody.valurex.ui.theme.ValurexColors
import com.nobody.valurex.ui.theme.ValurexTypography
import com.nobody.valurex.ui.theme.shapePill

@Composable
fun PillChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape    = shapePill,
        color    = Color.Transparent,
        border   = BorderStroke(
            width = 1.dp,
            color = if (selected) ValurexColors.ChipBorderActive else ValurexColors.ChipBorderInactive
        )
    ) {
        Text(
            text     = text,
            style    = ValurexTypography.chipText,
            color    = if (selected) ValurexColors.TextHigh else ValurexColors.TextMedium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}
