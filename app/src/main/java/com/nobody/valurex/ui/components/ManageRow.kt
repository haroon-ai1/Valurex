package com.nobody.valurex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.nobody.valurex.ui.theme.ValurexColors
import com.nobody.valurex.ui.theme.ValurexTypography
import com.nobody.valurex.ui.theme.iconSquircle
import com.nobody.valurex.ui.theme.shapeRowBottom
import com.nobody.valurex.ui.theme.shapeRowMiddle
import com.nobody.valurex.ui.theme.shapeRowSingle
import com.nobody.valurex.ui.theme.shapeRowTop

enum class RowPosition { TOP, MIDDLE, BOTTOM, SINGLE }

@Composable
fun ManageRow(
    leading: @Composable () -> Unit,
    title: String,
    subtitle: String,
    position: RowPosition,
    onClick: (() -> Unit)? = null,
    trailingIcon: ImageVector? = Icons.Filled.ChevronRight
) {
    val shape: Shape = when (position) {
        RowPosition.TOP    -> shapeRowTop
        RowPosition.MIDDLE -> shapeRowMiddle
        RowPosition.BOTTOM -> shapeRowBottom
        RowPosition.SINGLE -> shapeRowSingle
    }
    Column {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape    = shape,
            color    = ValurexColors.Surface
        ) {
            val rowMod = if (onClick != null)
                Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp)
            else
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)
            Row(modifier = rowMod, verticalAlignment = Alignment.CenterVertically) {
                leading()
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = ValurexTypography.rowTitle, color = ValurexColors.TextHigh)
                    if (subtitle.isNotEmpty()) {
                        Spacer(Modifier.height(2.dp))
                        Text(subtitle, style = ValurexTypography.rowSubtitle, color = ValurexColors.TextMedium)
                    }
                }
                if (trailingIcon != null) {
                    Spacer(Modifier.width(8.dp))
                    Icon(trailingIcon, null, tint = ValurexColors.TextLow, modifier = Modifier.size(18.dp))
                }
            }
        }
        if (position == RowPosition.TOP || position == RowPosition.MIDDLE) {
            Spacer(
                modifier = Modifier.fillMaxWidth().height(2.dp).background(ValurexColors.Background)
            )
        }
    }
}

@Composable
fun ManageRow(
    icon: ImageVector,
    iconBg: Color,
    iconFg: Color,
    title: String,
    subtitle: String,
    position: RowPosition,
    onClick: (() -> Unit)? = null,
    trailingIcon: ImageVector? = Icons.Filled.ChevronRight
) = ManageRow(
    leading = {
        Box(
            modifier         = Modifier.size(38.dp).background(iconBg, iconSquircle),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconFg, modifier = Modifier.size(18.dp))
        }
    },
    title        = title,
    subtitle     = subtitle,
    position     = position,
    onClick      = onClick,
    trailingIcon = trailingIcon
)
