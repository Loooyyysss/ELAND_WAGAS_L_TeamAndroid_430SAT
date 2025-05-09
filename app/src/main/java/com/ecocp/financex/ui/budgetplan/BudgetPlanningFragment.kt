package com.ecocp.financex.ui.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ecocp.financex.R
import com.ecocp.financex.data.BudgetPair
import com.ecocp.financex.ui.dialog.AddExpenseDialog
import com.ecocp.financex.ui.budgetplan.BudgetPairAdapter
import com.ecocp.financex.ui.dialog.EditExpenseDialog

class BudgetPlanningFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var totalBudgetText: TextView
    private lateinit var addButton: Button
    private lateinit var editButton: Button
    private lateinit var warningLayout: View

    private val budgetList = mutableListOf<BudgetPair>()
    private lateinit var adapter: BudgetPairAdapter

    private var totalDailyBudget = 0.0  // Start at 0.00

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_budget_planning, container, false)

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerViewBudgetPairs)
        totalBudgetText = view.findViewById(R.id.textTotalBudget)
        addButton = view.findViewById(R.id.buttonAdd)
        editButton = view.findViewById(R.id.buttonEdit)
        warningLayout = view.findViewById(R.id.warningLayout)

        val backButton = view.findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // RecyclerView setup
        adapter = BudgetPairAdapter(
            budgetList,
            onAddExpenseClicked = { pair -> showAddBudgetDialog(pair) },
            onEditExpenseClicked = { pair -> showEditExpenseDialog(pair) }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        updateTotalBudgetDisplay()

        addButton.setOnClickListener { openAddCategoryDialog() }
        editButton.setOnClickListener { openEditTotalBudgetDialog() }

        return view
    }


    private fun openAddCategoryDialog() {
        val dialog = AddExpenseDialog(requireContext()) { budgetAmount: Double, categoryName: String? ->
            val newItem = BudgetPair(categoryName ?: "Unknown", budgetAmount, 0.0)
            budgetList.add(newItem)
            adapter.notifyItemInserted(budgetList.size - 1)
            checkWarnings()
        }
        dialog.show()
    }

    private fun openEditTotalBudgetDialog() {
        val dialog = EditExpenseDialog(requireContext(), null) { newAmount ->
            totalDailyBudget = newAmount
            updateTotalBudgetDisplay()
            checkWarnings()
        }
        dialog.show()
    }

    private fun showAddBudgetDialog(pair: BudgetPair) {
        val dialog = AddExpenseDialog(requireContext()) { amount: Double, _ ->
            pair.budgetAmount += amount
            adapter.notifyDataSetChanged()
            checkWarnings()
        }
        dialog.show()
    }

    private fun showEditExpenseDialog(pair: BudgetPair) {
        val dialog = EditExpenseDialog(requireContext(), pair) { newExpense ->
            pair.expenseAmount = newExpense
            adapter.notifyDataSetChanged()
            checkWarnings()
        }
        dialog.show()
    }

    private fun updateTotalBudgetDisplay() {
        totalBudgetText.text = "₱%.2f".format(totalDailyBudget)
    }

    private fun checkWarnings() {
        val totalExpenses = budgetList.sumOf { it.expenseAmount }
        val warningTextView = warningLayout.findViewById<TextView>(R.id.warningText)

        val exceededCategories = budgetList.filter { it.expenseAmount > it.budgetAmount }

        if (::warningLayout.isInitialized) {
            if (exceededCategories.isNotEmpty() || totalExpenses > totalDailyBudget) {
                warningLayout.visibility = View.VISIBLE

                val messages = mutableListOf<String>()
                if (totalExpenses > totalDailyBudget) {
                    messages.add("⚠️ Total expenses exceeded your daily budget!")
                }
                messages.addAll(exceededCategories.map {
                    "⚠️ ${it.category} exceeded by ₱${"%.2f".format(it.expenseAmount - it.budgetAmount)}"
                })

                warningTextView.text = messages.joinToString("\n")
            } else {
                warningLayout.visibility = View.GONE
            }
        }
    }
}
