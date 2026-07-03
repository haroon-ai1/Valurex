package com.nobody.valurex.data.repo

import com.nobody.valurex.data.db.dao.LoanDao
import com.nobody.valurex.data.db.entity.Loan
import kotlinx.coroutines.flow.Flow

class LoanRepository(private val dao: LoanDao) {
    fun getAll(): Flow<List<Loan>> = dao.getAll()

    suspend fun insert(personName: String, amount: Int, direction: String, note: String?): Loan {
        val l = Loan(
            personName = personName, amount = amount, direction = direction,
            note = note, createdAt = System.currentTimeMillis()
        )
        return l.copy(id = dao.insert(l))
    }

    suspend fun markSettled(id: Long) = dao.markSettled(id, System.currentTimeMillis())

    suspend fun delete(id: Long) = dao.deleteById(id)
}
