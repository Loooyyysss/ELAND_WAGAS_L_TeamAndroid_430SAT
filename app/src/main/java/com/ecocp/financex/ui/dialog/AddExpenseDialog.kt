package com.ecocp.financex.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.ecocp.financex.R

class AddExpenseDialog(context: Context, val onExpenseAdded: (Double, String?) -> Unit) : Dialog(context) {

    private lateinit var amountEditText: EditText
    private lateinit var noteEditText: EditText
    private lateinit var confirmButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_add)
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        amountEditText = findViewById(R.id.editTextAmountAdd)
        noteEditText = findViewById(R.id.editTextNoteAdd)
        confirmButton = findViewById(R.id.buttonConfirmAdd)

        confirmButton.setOnClickListener {
            val amountText = amountEditText.text.toString()
            val noteText = noteEditText.text.toString()

            val amount = amountText.toDoubleOrNull()
            if (amount != null) {
                onExpenseAdded(amount, if (noteText.isEmpty()) null else noteText)
                dismiss()
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
