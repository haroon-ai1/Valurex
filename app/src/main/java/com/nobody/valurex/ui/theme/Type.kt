package com.nobody.valurex.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object ValurexTypography {
    val pageTitle = TextStyle(
        fontWeight    = FontWeight.Medium,
        fontSize      = 28.sp,
        lineHeight    = 34.sp,
        letterSpacing = (-0.5).sp
    )
    val heroNumber = TextStyle(
        fontWeight    = FontWeight.Medium,
        fontSize      = 44.sp,
        lineHeight    = 44.sp,
        letterSpacing = (-1.5).sp
    )
    val statTileNumber = TextStyle(
        fontWeight    = FontWeight.Medium,
        fontSize      = 22.sp,
        lineHeight    = 22.sp,
        letterSpacing = (-0.5).sp
    )
    val sectionLabel = TextStyle(
        fontWeight    = FontWeight.Medium,
        fontSize      = 11.sp,
        letterSpacing = 0.4.sp
    )
    val profileName = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 18.sp,
        lineHeight = 22.sp
    )
    val rowTitle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        lineHeight = 18.sp
    )
    val rowSubtitle = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 11.sp,
        lineHeight = 14.sp
    )
    val amount = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 14.sp
    )
    val chipText = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 11.sp
    )
    val captionTiny = TextStyle(
        fontWeight    = FontWeight.Normal,
        fontSize      = 10.sp,
        letterSpacing = 0.5.sp
    )
    val pillButton = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 12.sp
    )
}

val ValurexM3Typography = Typography(
    bodyLarge   = ValurexTypography.rowTitle,
    bodyMedium  = ValurexTypography.rowSubtitle,
    bodySmall   = ValurexTypography.captionTiny,
    titleMedium = ValurexTypography.amount,
    titleLarge  = ValurexTypography.pageTitle
)
