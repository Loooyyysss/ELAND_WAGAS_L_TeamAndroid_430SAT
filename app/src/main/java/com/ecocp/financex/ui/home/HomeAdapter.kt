package com.ecocp.financex.ui.home

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ecocp.financex.R
import com.ecocp.financex.data.Expense
import com.ecocp.financex.data.Home
import com.ecocp.financex.db.DatabaseHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeAdapter(
    private val dbHelper: DatabaseHelper,
    private val onDataChanged: () -> Unit
) : RecyclerView.Adapter<HomeAdapter.HomeViewHolder>() {

    private var homeItems: List<Home> = emptyList()

    fun submitList(newHomeItems: List<Home>) {
        homeItems = newHomeItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return HomeViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val homeItem = homeItems[position]
        holder.bind(homeItem)
    }

    override fun getItemCount(): Int = homeItems.size

    inner class HomeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryHeader: LinearLayout = itemView.findViewById(R.id.category_header)
        private val tvCategoryName: TextView = itemView.findViewById(R.id.tv_category_name)
        private val tvCategoryTotal: TextView = itemView.findViewById(R.id.tv_category_total)

        fun bind(homeItem: Home) {
            tvCategoryName.text = homeItem.category
            tvCategoryTotal.text = String.format(Locale.getDefault(), "₱%.2f", homeItem.totalAmount)

            categoryHeader.setOnClickListener {
                showExpenseDialog(homeItem)
            }
        }

        private fun showExpenseDialog(homeItem: Home) {
            val dialogView = LayoutInflater.from(itemView.context).inflate(R.layout.dialog_expense_list, null)
            val dialogTitle = dialogView.findViewById<TextView>(R.id.tv_dialog_title)
            val rvExpenseList = dialogView.findViewById<RecyclerView>(R.id.rv_expense_list)
            val btnClose = dialogView.findViewById<Button>(R.id.btn_close)

            dialogTitle.text = "Expenses for ${homeItem.category}"

            val expenseAdapter = ExpenseDialogAdapter(homeItem.expenses) { position, action ->
                when (action) {
                    "edit" -> showEditExpenseDialog(homeItem, position)
                    "delete" -> {
                        val expense = homeItem.expenses[position]
                        expense.id?.let { id ->
                            if (dbHelper.deleteExpense(id)) {
                                homeItem.expenses.removeAt(position)
                                homeItem.totalAmount = homeItem.expenses.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
                                notifyItemChanged(adapterPosition)
                                onDataChanged()
                            }
                        } ?: run {
                            val id = dbHelper.getExpenseId(
                                expense.expenseName,
                                expense.category,
                                expense.amount.toDoubleOrNull() ?: 0.0,
                                expense.date
                            )
                            if (id != null && dbHelper.deleteExpense(id)) {
                                homeItem.expenses.removeAt(position)
                                homeItem.totalAmount = homeItem.expenses.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
                                notifyItemChanged(adapterPosition)
                                onDataChanged()
                            }
                        }
                    }
                }
            }

            rvExpenseList.apply {
                layoutManager = LinearLayoutManager(itemView.context)
                adapter = expenseAdapter
            }

            val dialog = AlertDialog.Builder(itemView.context)
                .setView(dialogView)
                .create()

            btnClose.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }

        private fun showEditExpenseDialog(homeItem: Home, position: Int) {
            val expense = homeItem.expenses[position]
            val dialogView = LayoutInflater.from(itemView.context).inflate(R.layout.dialog_edit_expense, null)
            val etExpenseName = dialogView.findViewById<EditText>(R.id.et_expense_name)
            val etExpenseAmount = dialogView.findViewById<EditText>(R.id.et_expense_amount)
            val tvExpenseDate = dialogView.findViewById<TextView>(R.id.tv_expense_date)
            val etExpenseDescription = dialogView.findViewById<EditText>(R.id.et_expense_description)
            val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
            val btnSave = dialogView.findViewById<Button>(R.id.btn_save)

            // Pre-fill fields
            etExpenseName.setText(expense.expenseName)
            etExpenseAmount.setText(expense.amount)
            tvExpenseDate.text = expense.date
            etExpenseDescription.setText(expense.description)

            // Date picker
            tvExpenseDate.setOnClickListener {
                val calendar = Calendar.getInstance()
                try {
                    val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                    val date = sdf.parse(expense.date)
                    date?.let { calendar.time = it }
                } catch (e: Exception) {
                    // Fallback to current date if parsing fails
                }

                DatePickerDialog(
                    itemView.context,
                    { _, year, month, dayOfMonth ->
                        calendar.set(year, month, dayOfMonth)
                        val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                        tvExpenseDate.text = sdf.format(calendar.time)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

            val dialog = AlertDialog.Builder(itemView.context)
                .setView(dialogView)
                .create()

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            btnSave.setOnClickListener {
                val newName = etExpenseName.text.toString().trim()
                val newAmount = etExpenseAmount.text.toString().toDoubleOrNull() ?: 0.0
                val newDate = tvExpenseDate.text.toString()
                val newDescription = etExpenseDescription.text.toString().trim()

                android.util.Log.d("HomeAdapter", "Edit attempt - Name: $newName, Amount: $newAmount, Date: $newDate, Desc: $newDescription")

                if (newName.isEmpty() || newAmount <= 0.0 || newDate.isEmpty()) {
                    android.util.Log.d("HomeAdapter", "Validation failed")
                    return@setOnClickListener
                }

                expense.id?.let { id ->
                    android.util.Log.d("HomeAdapter", "Updating with ID: $id")
                    if (dbHelper.updateExpense(id, newName, newAmount, newDate, newDescription)) {
                        android.util.Log.d("HomeAdapter", "Update successful")
                        val updatedExpense = expense.copy(
                            expenseName = newName,
                            amount = newAmount.toString(),
                            date = newDate,
                            description = newDescription
                        )
                        homeItem.expenses[position] = updatedExpense
                        homeItem.totalAmount = homeItem.expenses.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
                        notifyItemChanged(adapterPosition)
                        onDataChanged()
                    } else {
                        android.util.Log.d("HomeAdapter", "Update failed with ID: $id")
                    }
                } ?: run {
                    android.util.Log.d("HomeAdapter", "No ID, using getExpenseId")
                    val id = dbHelper.getExpenseId(
                        expense.expenseName,
                        expense.category,
                        expense.amount.toDoubleOrNull() ?: 0.0,
                        expense.date
                    )
                    id?.let { existingId ->
                        android.util.Log.d("HomeAdapter", "Found ID: $existingId")
                        if (dbHelper.updateExpense(existingId, newName, newAmount, newDate, newDescription)) {
                            android.util.Log.d("HomeAdapter", "Update successful with fallback ID")
                            val updatedExpense = expense.copy(
                                id = existingId,
                                expenseName = newName,
                                amount = newAmount.toString(),
                                date = newDate,
                                description = newDescription
                            )
                            homeItem.expenses[position] = updatedExpense
                            homeItem.totalAmount = homeItem.expenses.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
                            notifyItemChanged(adapterPosition)
                            onDataChanged()
                        } else {
                            android.util.Log.d("HomeAdapter", "Update failed with fallback ID: $existingId")
                        }
                    } ?: run {
                        android.util.Log.d("HomeAdapter", "No ID found with getExpenseId")
                    }
                }
                dialog.dismiss()
            }

            dialog.show()
        }
    }
}

class ExpenseDialogAdapter(
    private val expenses: MutableList<Expense>,
    private val onAction: (Int, String) -> Unit
) : RecyclerView.Adapter<ExpenseDialogAdapter.ExpenseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense_dialog, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.bind(expense, position)
    }

    override fun getItemCount(): Int = expenses.size

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvExpenseName: TextView = itemView.findViewById(R.id.tv_expense_name)
        private val tvExpenseAmount: TextView = itemView.findViewById(R.id.tv_expense_amount)
        private val tvExpenseDate: TextView = itemView.findViewById(R.id.tv_expense_date)
        private val btnEdit: Button = itemView.findViewById(R.id.btn_edit)
        private val btnDelete: Button = itemView.findViewById(R.id.btn_delete)

        fun bind(expense: Expense, position: Int) {
            tvExpenseName.text = expense.expenseName
            tvExpenseAmount.text = String.format(Locale.getDefault(), "₱%.2f", expense.amount.toDoubleOrNull() ?: 0.0)
            tvExpenseDate.text = expense.date

            btnEdit.setOnClickListener {
                onAction(position, "edit")
            }

            btnDelete.setOnClickListener {
                onAction(position, "delete")
            }
        }
    }
}