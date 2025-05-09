package com.ecocp.financex.data

data class Expense(
    val id: Long? = null, // Add ID field for database reference
    val expenseName: String,
    val category: String,
    val amount: String,
    val date: String,
    val description: String
)