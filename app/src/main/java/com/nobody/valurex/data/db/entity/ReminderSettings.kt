package com.nobody.valurex.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminder_settings")
data class ReminderSettings(
    @PrimaryKey val id: Int = 1,
    val nightly_enabled: Boolean = false,
    val nightly_hour: Int = 22,
    val nightly_minute: Int = 0,
    val daily1_enabled: Boolean = false,
    val daily1_hour: Int = 12,
    val daily1_minute: Int = 0,
    val daily2_enabled: Boolean = false,
    val daily2_hour: Int = 18,
    val daily2_minute: Int = 0,
    val daily3_enabled: Boolean = false,
    val daily3_hour: Int = 21,
    val daily3_minute: Int = 0
)
