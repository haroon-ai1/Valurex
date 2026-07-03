package com.nobody.valurex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nobody.valurex.data.db.entity.Category
import com.nobody.valurex.ui.theme.ValurexColors
import com.nobody.valurex.ui.theme.iconSquircle

@Composable
fun CategoryIcon(
    category: Category,
    size: Dp = 38.dp
) {
    val (bg, fg, icon) = categoryStyle(category.name, category.color)
    Box(
        modifier         = Modifier
            .size(size)
            .background(bg, iconSquircle),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = fg,
            modifier           = Modifier.size(size * 0.5f)
        )
    }
}

private fun categoryStyle(name: String, color: String): Triple<Color, Color, ImageVector> {
    val lower = name.lowercase()
    return when {
        lower.containsAny("food", "restaurant", "eat", "dining", "biryani", "lunch", "dinner", "cafe", "snack") ->
            Triple(ValurexColors.IconBgFood, ValurexColors.IconFgFood, Icons.Outlined.Restaurant)
        lower.containsAny("transport", "car", "uber", "taxi", "fuel", "petrol", "bus", "ride", "travel") ->
            Triple(ValurexColors.IconBgTransport, ValurexColors.IconFgTransport, Icons.Outlined.DirectionsCar)
        lower.containsAny("bill", "util", "electric", "water", "internet", "phone", "subscription", "rent") ->
            Triple(ValurexColors.IconBgBills, ValurexColors.IconFgBills, Icons.Outlined.Receipt)
        lower.containsAny("income", "salary", "invest", "saving", "earning", "revenue") ->
            Triple(ValurexColors.IconBgIncome, ValurexColors.IconFgIncome, Icons.Outlined.TrendingUp)
        else -> {
            val catColor = runCatching {
                Color(android.graphics.Color.parseColor(color))
            }.getOrElse { ValurexColors.TextMedium }
            Triple(catColor.copy(alpha = 0.10f), catColor, Icons.Outlined.Category)
        }
    }
}

private fun String.containsAny(vararg words: String) = words.any { this.contains(it) }
