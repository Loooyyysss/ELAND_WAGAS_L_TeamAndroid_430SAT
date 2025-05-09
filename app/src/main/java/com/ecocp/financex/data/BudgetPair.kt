package com.ecocp.financex.data

data class BudgetPair(
    val category: String,
    var budgetAmount: Double,   // ✅ make mutable
    var expenseAmount: Double   // ✅ make mutable
)



