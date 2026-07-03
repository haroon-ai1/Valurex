package com.nobody.valurex.ui.theme

import androidx.compose.ui.graphics.Color

object ValurexColors {
    val Background        = Color(0xFF0A0A0A)
    val Surface           = Color(0xFF141414)
    val SurfaceElevated   = Color(0xFF1A1A1A)
    val Hairline          = Color(0xFF1F1F1F)
    val HairlineSubtle    = Color(0xFF161616)
    val TextHigh          = Color(0xFFFFFFFF)
    val TextMedium        = Color(0xFF8A8A8A)
    val TextLow           = Color(0xFF5A5A5A)
    val TextPlaceholder   = Color(0xFF5A5A5A)
    val Accent            = Color(0xFF8C52FF)
    val IncomeGreen       = Color(0xFF4ADE80)
    val ExpenseRed        = Color(0xFFF87171)
    val ChipBorderActive  = Color(0xFF2A2A2A)
    val ChipBorderInactive = Color(0xFF1F1F1F)

    val IconBgFood        = Color(0xFF1A1410)
    val IconBgTransport   = Color(0xFF0F1620)
    val IconBgBills       = Color(0xFF1A1014)
    val IconBgIncome      = Color(0xFF0F1A12)
    val IconBgMisc        = Color(0xFF161616)
    val IconBgBudget      = Color(0xFF1A1422)
    val IconBgRecurring   = Color(0xFF1A1410)
    val IconBgCategories  = Color(0xFF0F1A12)
    val IconBgEmail       = Color(0xFF0F1620)
    val IconBgLinkedIn    = Color(0xFF0F1620)
    val IconBgInfo        = Color(0xFF141414)

    val IconFgFood        = Color(0xFFE89A5C)
    val IconFgTransport   = Color(0xFF5C9FE8)
    val IconFgBills       = Color(0xFFE85C7A)
    val IconFgIncome      = Color(0xFF4ADE80)
    val IconFgMisc        = Color(0xFF8A8A8A)
}

// Backward-compat aliases (used by AddManualScreen from Chunk 6)
val Accent         = ValurexColors.Accent
val TextHigh       = ValurexColors.TextHigh
val TextMedium     = ValurexColors.TextMedium
val TextInactive   = ValurexColors.TextPlaceholder
val TextLow        = ValurexColors.TextLow
val SurfaceVariant = ValurexColors.SurfaceElevated
val InputBorder    = ValurexColors.Hairline
val Background     = ValurexColors.Background
val Surface        = ValurexColors.Surface
val IncomeGreen    = ValurexColors.IncomeGreen
val ExpenseRed     = ValurexColors.ExpenseRed
val Divider        = ValurexColors.HairlineSubtle
