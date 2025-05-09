package com.ecocp.financex.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ecocp.financex.R
import com.ecocp.financex.data.Expense
import com.ecocp.financex.data.Home
import com.ecocp.financex.db.DatabaseHelper
import com.ecocp.financex.ui.budget.BudgetPlanningFragment
import com.ecocp.financex.ui.cat_and_exp.CategoryFragment
import com.ecocp.financex.ui.cat_and_exp.ExpenseFragment
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var profileImage: CircleImageView
    private lateinit var expenseTotal: TextView
    private lateinit var hideExpenseToggle: ImageView
    private lateinit var reportStatus: TextSwitcher
    private lateinit var reportRecyclerView: RecyclerView
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var homeAdapter: HomeAdapter

    private var isExpenseHidden = false
    private var currentPeriod = "This week" // Default period

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI components
        profileImage = view.findViewById(R.id.user_profile_image_holder)
        expenseTotal = view.findViewById(R.id.expense_total)
        hideExpenseToggle = view.findViewById(R.id.hide_expense)
        reportStatus = view.findViewById(R.id.reportgenstatus)
        reportRecyclerView = view.findViewById(R.id.report_recycler_view)

        // Initialize DB Helper
        dbHelper = DatabaseHelper(requireContext())

        // Initialize menu options
        setupMenuOptions(view)

        // Setup profile image click
        profileImage.setOnClickListener {
            Toast.makeText(requireContext(), "Profile clicked", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to profile screen if needed
        }

        // Setup hide/show expense toggle
        setupExpenseToggle()

        // Setup TextSwitcher
        setupTextSwitcher()

        // Setup RecyclerView
        setupRecyclerView()

        // Load initial data
        updateExpenseTotal()
        updateReportData(currentPeriod)
    }

    private fun setupMenuOptions(view: View) {
        val budgetOption = view.findViewById<LinearLayout>(R.id.opt_budget)
        val expenseOption = view.findViewById<LinearLayout>(R.id.opt_expense)
        val categoryOption = view.findViewById<LinearLayout>(R.id.opt_category)

        budgetOption.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BudgetPlanningFragment())
                .addToBackStack(null)
                .commit()
        }

        expenseOption.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ExpenseFragment())
                .addToBackStack(null)
                .commit()
        }

        categoryOption.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CategoryFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupExpenseToggle() {
        hideExpenseToggle.setOnClickListener {
            isExpenseHidden = !isExpenseHidden
            if (isExpenseHidden) {
                expenseTotal.text = "****"
                hideExpenseToggle.setImageResource(R.drawable.ic_showexpense_24)
            } else {
                updateExpenseTotal()
                hideExpenseToggle.setImageResource(R.drawable.witness)
            }
        }
    }

    private fun setupTextSwitcher() {
        reportStatus.setFactory {
            TextView(requireContext()).apply {
                textSize = 10f
                setPadding(6, 6, 6, 6)
                setBackgroundResource(R.drawable.layout_roundfieldnotfilled)
                setTextColor(resources.getColor(R.color.black, null))
            }
        }
        reportStatus.setText(currentPeriod)
        reportStatus.setOnClickListener {
            currentPeriod = when (currentPeriod) {
                "This week" -> "This month"
                "This month" -> "This week"
                else -> "This week"
            }
            reportStatus.setText(currentPeriod)
            updateReportData(currentPeriod)
        }
    }

    private fun setupRecyclerView() {
        homeAdapter = HomeAdapter(dbHelper) {
            // Callback to refresh data after edit/delete
            updateExpenseTotal()
            updateReportData(currentPeriod)
        }
        reportRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = homeAdapter
        }
    }

    private fun updateExpenseTotal() {
        val total = getTotalExpensesForPeriod(currentPeriod)
        expenseTotal.text = String.format(Locale.getDefault(), "₱%.2f", total)
    }

    private fun getTotalExpensesForPeriod(period: String): Double {
        val db = dbHelper.readableDatabase
        val cursor = when (period) {
            "This week" -> db.rawQuery("SELECT SUM(amount) FROM expenses WHERE date >= date('now', '-7 days')", null)
            "This month" -> db.rawQuery("SELECT SUM(amount) FROM expenses WHERE date >= date('now', '-30 days')", null)
            else -> db.rawQuery("SELECT SUM(amount) FROM expenses", null)
        }
        var total = 0.0
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0) ?: 0.0
        }
        cursor.close()
        return total
    }

    private fun updateReportData(period: String) {
        val homeItems = getExpensesByCategoryForPeriod(period)
        homeAdapter.submitList(homeItems)
    }

    private fun getExpensesByCategoryForPeriod(period: String): List<Home> {
        val db = dbHelper.readableDatabase
        val categoryMap = mutableMapOf<String, Home>()
        val cursor = when (period) {
            "This week" -> db.rawQuery("SELECT * FROM expenses WHERE date >= date('now', '-7 days')", null)
            "This month" -> db.rawQuery("SELECT * FROM expenses WHERE date >= date('now', '-30 days')", null)
            else -> db.rawQuery("SELECT * FROM expenses", null)
        }
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val category = cursor.getString(cursor.getColumnIndexOrThrow("category"))
                val amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"))
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                val date = cursor.getString(cursor.getColumnIndexOrThrow("date"))
                val description = cursor.getString(cursor.getColumnIndexOrThrow("description")) ?: ""
                val expense = Expense(id, name, category, amount.toString(), date, description)

                val homeItem = categoryMap.getOrPut(category) { Home(category, 0.0, mutableListOf()) }
                homeItem.totalAmount += amount
                homeItem.expenses.add(expense)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return categoryMap.values.toList()
    }
}