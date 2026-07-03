package com.nobody.valurex.data.repo

import com.nobody.valurex.data.db.dao.CategoryDao
import com.nobody.valurex.data.db.entity.Category
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {
    fun getAll(): Flow<List<Category>> = categoryDao.getAll()

    suspend fun insertCategory(name: String, color: String, monthlyLimit: Int?): Category {
        val c = Category(name = name, color = color, monthlyLimit = monthlyLimit, isDefault = false)
        return c.copy(id = categoryDao.insert(c))
    }

    suspend fun updateCategory(category: Category) = categoryDao.update(category)

    suspend fun deleteCategory(category: Category): Boolean {
        if (category.isDefault) return false
        categoryDao.delete(category)
        return true
    }
}
