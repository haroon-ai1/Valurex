package com.nobody.valurex.parser

import org.junit.Assert.*
import org.junit.Test

class TransactionParserTest {
    @Test fun `extracts amount from end`() = assertEquals(350, TransactionParser.extractAmount("biryani 350"))
    @Test fun `extracts amount from start`() = assertEquals(200, TransactionParser.extractAmount("200 uber"))
    @Test fun `extracts first of multiple numbers`() = assertEquals(500, TransactionParser.extractAmount("500 uber 200"))
    @Test fun `returns null when no number`() = assertNull(TransactionParser.extractAmount("biryani only"))
    @Test fun `remaining strips digits and trims`() = assertEquals("biryani", TransactionParser.extractRemaining("biryani 350"))
    @Test fun `remaining handles leading number`() = assertEquals("uber", TransactionParser.extractRemaining("200 uber"))
    @Test fun `tokenize lowercases`() = assertEquals(listOf("uber", "to", "airport"), TransactionParser.tokenize("Uber to Airport"))
    @Test fun `tokenize filters blanks`() = assertEquals(listOf("chai"), TransactionParser.tokenize("  chai  "))
}
