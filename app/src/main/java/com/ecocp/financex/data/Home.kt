package com.ecocp.financex.data

data class Home(
    val category: String,
    var totalAmount: Double,
    val expenses: MutableList<Expense>
)