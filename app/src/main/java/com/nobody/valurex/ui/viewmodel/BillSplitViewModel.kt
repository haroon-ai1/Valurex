package com.nobody.valurex.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nobody.valurex.ValurexApplication
import com.nobody.valurex.data.db.entity.Loan
import com.nobody.valurex.parser.TransactionType
import kotlinx.coroutines.launch

data class LoanConflict(
    val name: String,
    val existingAmount: Int,
    val newShare: Int,
    val onDecision: (Boolean) -> Unit // true = Add to existing, false = Create Name 2
)

class BillSplitViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as ValurexApplication
    private val loanRepo = app.loanRepository
    private val transRepo = app.transactionRepository
    private val db = app.database
    private val loanDao = db.loanDao()
    private val categoryDao = db.categoryDao()

    fun createSplit(
        totalAmount: Int,
        numPeople: Int,
        iPaid: Boolean,
        whoPaidName: String?, // Only if !iPaid
        otherPeopleNames: List<String>, // Only if iPaid
        onConflict: (LoanConflict) -> Unit,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val share = totalAmount / numPeople
            if (!iPaid) {
                // Someone else paid, I owe them my share
                val name = whoPaidName?.trim() ?: "Unknown"
                val existing = loanDao.findActiveByName(name, "I_OWE")
                if (existing != null) {
                    onConflict(LoanConflict(name, existing.amount, share) { addToExisting ->
                        viewModelScope.launch {
                            if (addToExisting) {
                                loanDao.update(existing.copy(amount = existing.amount + share))
                            } else {
                                loanRepo.insert(name + " 2", share, "I_OWE", "Bill split: $totalAmount total, $numPeople people")
                            }
                            onComplete()
                        }
                    })
                } else {
                    loanRepo.insert(name, share, "I_OWE", "Bill split: $totalAmount total, $numPeople people")
                    onComplete()
                }
            } else {
                // I paid, others owe me their share
                // Log my share as expense
                val misc = categoryDao.getByName("Misc")
                transRepo.insertTransaction(
                    amount = share,
                    categoryId = misc?.id ?: 1L,
                    note = "Bill split — my share",
                    source = "MANUAL",
                    type = TransactionType.EXPENSE
                )

                val conflicts = mutableListOf<String>()
                val decisions = mutableMapOf<String, Boolean>()
                
                // We need to handle conflicts one by one
                processNextPerson(otherPeopleNames, share, totalAmount, numPeople, onConflict, onComplete)
            }
        }
    }

    private fun processNextPerson(
        names: List<String>, share: Int, total: Int, count: Int,
        onConflict: (LoanConflict) -> Unit, onComplete: () -> Unit
    ) {
        if (names.isEmpty()) {
            onComplete()
            return
        }
        val name = names.first().trim()
        if (name.isEmpty()) {
            processNextPerson(names.drop(1), share, total, count, onConflict, onComplete)
            return
        }

        viewModelScope.launch {
            val existing = loanDao.findActiveByName(name, "OWED_TO_ME")
            if (existing != null) {
                onConflict(LoanConflict(name, existing.amount, share) { addToExisting ->
                    viewModelScope.launch {
                        if (addToExisting) {
                            loanDao.update(existing.copy(amount = existing.amount + share))
                        } else {
                            loanRepo.insert(name + " 2", share, "OWED_TO_ME", "Bill split: $total total, $count ppl")
                        }
                        processNextPerson(names.drop(1), share, total, count, onConflict, onComplete)
                    }
                })
            } else {
                loanRepo.insert(name, share, "OWED_TO_ME", "Bill split: $total, $count ppl")
                processNextPerson(names.drop(1), share, total, count, onConflict, onComplete)
            }
        }
    }
}
