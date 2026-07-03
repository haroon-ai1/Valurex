package com.nobody.valurex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nobody.valurex.ui.theme.ValurexColors
import com.nobody.valurex.ui.theme.ValurexTypography
import com.nobody.valurex.ui.theme.shapeMedium

@Composable
fun StatTile(
    label: String,
    value: String,
    valueColor: Color = ValurexColors.TextHigh,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(ValurexColors.Surface, shapeMedium)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text  = label.uppercase(),
            style = ValurexTypography.sectionLabel,
            color = ValurexColors.TextMedium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text  = value,
            style = ValurexTypography.statTileNumber,
            color = valueColor
        )
    }
}
