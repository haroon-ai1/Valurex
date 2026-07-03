package com.nobody.valurex.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nobody.valurex.ValurexApplication
import com.nobody.valurex.data.db.entity.Loan
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LoansViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as ValurexApplication).loanRepository

    val loans: StateFlow<List<Loan>> = repo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addLoan(personName: String, amount: Int, direction: String, note: String?) {
        viewModelScope.launch { repo.insert(personName, amount, direction, note) }
    }

    fun markSettled(id: Long) {
        viewModelScope.launch { repo.markSettled(id) }
    }

    fun deleteLoan(id: Long) {
        viewModelScope.launch { repo.delete(id) }
    }
}
