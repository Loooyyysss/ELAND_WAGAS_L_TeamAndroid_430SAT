package com.ecocp.financex.ui.cat_and_exp

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.ecocp.financex.R
import com.ecocp.financex.data.Expense
import com.ecocp.financex.db.DatabaseHelper
import java.text.SimpleDateFormat
import java.util.*

class ExpenseFragment : Fragment() {

    private lateinit var etExpenseName: EditText
    private lateinit var spCategory: Spinner
    private lateinit var etAmount: EditText
    private lateinit var etDate: TextView
    private lateinit var etDescription: EditText
    private lateinit var btnSubmit: Button
    private lateinit var tvAddInitialCat: TextView
    private lateinit var btnBack: ImageButton

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_expense, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI components
        etExpenseName = view.findViewById(R.id.expensenameedittext)
        spCategory = view.findViewById(R.id.expensecategoryspinner)
        etAmount = view.findViewById(R.id.expenseamountedittext)
        etDate = view.findViewById(R.id.datepurchasededittext)
        etDescription = view.findViewById(R.id.expensedescedittext)
        btnSubmit = view.findViewById(R.id.expensesubmitbutton)
        tvAddInitialCat = view.findViewById(R.id.AddInitialCat)
        btnBack = view.findViewById(R.id.expensebackbutton)

        // Initialize DB Helper
        dbHelper = DatabaseHelper(requireContext())

        // Set up date picker
        setupDatePicker()

        // Set up back button
        setupBackButton()

        // Load categories into spinner
        loadCategories()

        btnSubmit.setOnClickListener {
            saveExpense()
        }
    }

    private fun setupDatePicker() {
        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    // Format the selected date (e.g., "2025-05-09")
                    val selectedDate = Calendar.getInstance().apply {
                        set(selectedYear, selectedMonth, selectedDay)
                    }
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    etDate.text = sdf.format(selectedDate.time)
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }
    }

    private fun setupBackButton() {
        btnBack.setOnClickListener {
            // Navigate back by popping the fragment from the back stack
            parentFragmentManager.popBackStack()
        }
    }

    private fun loadCategories() {
        val categories = dbHelper.getAllCategories()
        if (categories.isEmpty()) {
            tvAddInitialCat.visibility = View.VISIBLE
            spCategory.visibility = View.GONE
            Toast.makeText(requireContext(), "No categories found. Please add one first.", Toast.LENGTH_SHORT).show()
        } else {
            tvAddInitialCat.visibility = View.GONE
            spCategory.visibility = View.VISIBLE

            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spCategory.adapter = adapter
        }
    }

    private fun saveExpense() {
        val name = etExpenseName.text.toString().trim()
        val category = spCategory.selectedItem?.toString()?.trim()
        val amount = etAmount.text.toString().trim()
        val date = etDate.text.toString().trim()
        val description = etDescription.text.toString().trim()

        if (name.isEmpty() || category.isNullOrEmpty() || amount.isEmpty() || date.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all required fields.", Toast.LENGTH_SHORT).show()
            return
        }

        val amountValue = try {
            amount.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Invalid amount format.", Toast.LENGTH_SHORT).show()
            return
        }

        val expense = Expense(
            expenseName = name,
            category = category,
            amount = amountValue.toString(),
            date = date,
            description = description
        )

        val success = dbHelper.insertExpense(expense)

        if (success) {
            Toast.makeText(requireContext(), "Expense saved successfully!", Toast.LENGTH_SHORT).show()
            clearFields()
        } else {
            Toast.makeText(requireContext(), "Failed to save expense.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearFields() {
        etExpenseName.text.clear()
        etAmount.text.clear()
        etDate.text = ""
        etDescription.text.clear()
        spCategory.setSelection(0)
    }
}