package com.nobody.valurex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nobody.valurex.ui.theme.ValurexColors
import com.nobody.valurex.ui.theme.shapeMedium
import com.nobody.valurex.ui.theme.shapeSmall

@Composable
fun SavingsBar(
    savedAmount: Int,
    percentLess: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vc    = ValurexColors
    val green = Color(0xFF4ADE80)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, green.copy(alpha = 0.20f), shapeMedium),
        shape = shapeMedium,
        color = vc.Surface
    ) {
        Box(modifier = Modifier.background(green.copy(alpha = 0.08f))) {
            Row(
                modifier          = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier         = Modifier
                        .size(36.dp)
                        .background(green.copy(alpha = 0.15f), shapeSmall),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.TrendingDown,
                        contentDescription = null,
                        tint               = green,
                        modifier           = Modifier.size(18.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = "You saved Rs ${"%,d".format(savedAmount)} this week",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color      = vc.TextHigh,
                        lineHeight = (13 * 1.3).sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text     = "$percentLess% less spending than last week",
                        fontSize = 11.sp,
                        color    = vc.TextMedium
                    )
                }

                Spacer(Modifier.width(12.dp))

                IconButton(
                    onClick  = onDismiss,
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint               = vc.TextMedium.copy(alpha = 0.5f),
                        modifier           = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
