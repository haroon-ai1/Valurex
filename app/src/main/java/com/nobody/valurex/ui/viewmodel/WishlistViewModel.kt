package com.nobody.valurex.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nobody.valurex.ValurexApplication
import com.nobody.valurex.data.db.entity.WishlistItem
import com.nobody.valurex.ui.util.CopyImageToInternal
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WishlistViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as ValurexApplication).wishlistRepository

    val items: StateFlow<List<WishlistItem>> = repo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val count: StateFlow<Int> = repo.getCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val total: StateFlow<Int> = repo.getTotal()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun addWish(name: String, price: Int?, note: String?, imageUri: String?) {
        viewModelScope.launch {
            try {
                repo.insert(WishlistItem(name = name, price = price, note = note?.ifBlank { null }, imageUri = imageUri))
            } catch (_: Exception) {}
        }
    }

    fun updateWish(item: WishlistItem) {
        viewModelScope.launch {
            try { repo.update(item) } catch (_: Exception) {}
        }
    }

    fun deleteWish(item: WishlistItem) {
        viewModelScope.launch {
            try {
                repo.delete(item)
                item.imageUri?.let { CopyImageToInternal.delete(it) }
            } catch (_: Exception) {}
        }
    }
}
