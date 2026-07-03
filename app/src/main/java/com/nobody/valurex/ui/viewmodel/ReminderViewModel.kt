package com.nobody.valurex.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nobody.valurex.ValurexApplication
import com.nobody.valurex.data.db.entity.ReminderSettings
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ReminderViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as ValurexApplication).reminderRepository

    val settings: StateFlow<ReminderSettings> = repo.getSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReminderSettings())

    fun updateNightly(enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch {
            repo.updateNightly(enabled, hour, minute, settings.value)
        }
    }

    fun updateDaily(slot: Int, enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch {
            repo.updateDaily(slot, enabled, hour, minute, settings.value)
        }
    }
}
