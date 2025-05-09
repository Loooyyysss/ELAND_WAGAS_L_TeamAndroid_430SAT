package com.ecocp.financex.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.ecocp.financex.R
import com.ecocp.financex.data.BudgetPair

class EditExpenseDialog(
    context: Context,
    private val pair: BudgetPair?,  // Now nullable
    private val onExpenseUpdated: (Double) -> Unit
) : Dialog(context) {

    private lateinit var amountEditText: EditText
    private lateinit var updateButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_edit)
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        amountEditText = findViewById(R.id.amountEditText)
        updateButton = findViewById(R.id.updateButton)

        // Set initial value if pair is provided, otherwise default to empty
        amountEditText.setText(pair?.expenseAmount?.toString() ?: "")

        updateButton.setOnClickListener {
            val amount = amountEditText.text.toString().toDoubleOrNull()
            if (amount != null) {
                onExpenseUpdated(amount)
                dismiss()
            } else {
                amountEditText.error = "Enter a valid amount"
            }
        }
    }
    override fun onStart() {
        super.onStart()
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.90).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

}
