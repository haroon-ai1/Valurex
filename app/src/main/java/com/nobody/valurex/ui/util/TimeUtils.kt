package com.nobody.valurex.ui.util

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

fun formatRelativeTime(timestamp: Long): String {
    val diff    = System.currentTimeMillis() - timestamp
    val minutes = diff / 60_000
    val hours   = minutes / 60
    val days    = hours / 24
    return when {
        minutes < 1  -> "just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24   -> "${hours}h ago"
        days == 1L   -> "yesterday"
        else -> {
            val d = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
            "${d.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())} ${d.dayOfMonth}"
        }
    }
}

fun currentWeekStartMillis(): Long {
    val zone   = ZoneId.systemDefault()
    val monday = LocalDate.now(zone).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    return monday.atStartOfDay(zone).toInstant().toEpochMilli()
}

fun currentMonthStartMillis(): Long {
    val zone = ZoneId.systemDefault()
    return LocalDate.now(zone).withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
}

fun last30DaysStartMillis(): Long {
    val zone = ZoneId.systemDefault()
    return LocalDate.now(zone).minusDays(29).atStartOfDay(zone).toInstant().toEpochMilli()
}
