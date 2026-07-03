package com.nobody.valurex.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val shapeSmall    = RoundedCornerShape(12.dp)
val shapeMedium   = RoundedCornerShape(16.dp)
val shapeLarge    = RoundedCornerShape(20.dp)
val shapePill     = RoundedCornerShape(100.dp)
val shapeRowTop   = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
val shapeRowMiddle = RoundedCornerShape(4.dp)
val shapeRowBottom = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
val shapeRowSingle = RoundedCornerShape(16.dp)
val iconSquircle  = RoundedCornerShape(12.dp)

val ValurexShapes = Shapes(small = shapeSmall, medium = shapeMedium, large = shapeLarge)

// Backward-compat alias (used by AddManualScreen from Chunk 6)
val PillShape = shapePill
