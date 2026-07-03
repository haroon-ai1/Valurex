package com.nobody.valurex.parser

import com.nobody.valurex.data.db.dao.KeywordMapDao

sealed class ParserResult {
    data class Matched(val amount: Int, val categoryId: Long) : ParserResult()
    data class Unmatched(val amount: Int, val unknownText: String) : ParserResult()
    object NoAmount : ParserResult()
}

class TransactionParser(private val keywordMapDao: KeywordMapDao) {

    suspend fun parse(input: String): ParserResult {
        val amount = extractAmount(input) ?: return ParserResult.NoAmount
        val remaining = extractRemaining(input)
        for (token in tokenize(remaining)) {
            keywordMapDao.lookup(token)?.let { return ParserResult.Matched(amount, it.id) }
        }
        return ParserResult.Unmatched(amount, remaining.ifBlank { input.trim() })
    }

    companion object {
        fun extractAmount(text: String): Int? = Regex("\\d+").find(text)?.value?.toInt()
        fun extractRemaining(text: String): String = text.replace(Regex("\\d+"), "").trim()
        fun tokenize(text: String): List<String> =
            text.split(Regex("\\s+")).filter { it.isNotBlank() }.map { it.lowercase() }

        private val INCOME_TOKENS = setOf(
            "salary", "pocket", "allowance", "gift", "income",
            "received", "freelance", "payment"
        )

        fun detectType(text: String): TransactionType {
            val lower = text.lowercase()
            return if (INCOME_TOKENS.any { lower.contains(it) }) TransactionType.INCOME
            else TransactionType.EXPENSE
        }
    }
}
