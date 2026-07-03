package com.nobody.valurex.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nobody.valurex.ValurexApplication
import com.nobody.valurex.data.db.entity.Category
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CategoriesViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as ValurexApplication).categoryRepository

    val categories: StateFlow<List<Category>> = repo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun insertCategory(name: String, color: String, monthlyLimit: Int?) {
        viewModelScope.launch { repo.insertCategory(name, color, monthlyLimit) }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch { repo.updateCategory(category) }
    }

    fun deleteCategory(category: Category, onBlocked: () -> Unit) {
        viewModelScope.launch { if (!repo.deleteCategory(category)) onBlocked() }
    }
}
