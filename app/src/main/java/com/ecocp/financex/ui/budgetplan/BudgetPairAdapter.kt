package com.ecocp.financex.ui.budgetplan

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecocp.financex.R
import com.ecocp.financex.data.BudgetPair

class BudgetPairAdapter(
    private val budgetList: MutableList<BudgetPair>,
    private val onAddExpenseClicked: (BudgetPair) -> Unit,
    private val onEditExpenseClicked: (BudgetPair) -> Unit
) : RecyclerView.Adapter<BudgetPairAdapter.BudgetPairViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetPairViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_budget_pair, parent, false)
        return BudgetPairViewHolder(view)
    }

    override fun onBindViewHolder(holder: BudgetPairViewHolder, position: Int) {
        val item = budgetList[position]
        holder.bind(item)

        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete '${item.category}'?")
                .setPositiveButton("Delete") { _, _ ->
                    if (holder.adapterPosition != RecyclerView.NO_POSITION) {
                        budgetList.removeAt(holder.adapterPosition)
                        notifyItemRemoved(holder.adapterPosition)
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
            true
        }

        holder.addButton.setOnClickListener { onAddExpenseClicked(item) }
        holder.editButton.setOnClickListener { onEditExpenseClicked(item) }
    }

    override fun getItemCount(): Int = budgetList.size

    inner class BudgetPairViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val addButton: Button = itemView.findViewById(R.id.buttonAddBudget)
        val editButton: Button = itemView.findViewById(R.id.buttonEditExpense)

        fun bind(pair: BudgetPair) {
            itemView.findViewById<TextView>(R.id.textBudgetAmount).text =
                "₱%.2f".format(pair.budgetAmount)
            itemView.findViewById<TextView>(R.id.textExpenseAmount).text =
                "₱%.2f".format(pair.expenseAmount)

            itemView.findViewById<TextView>(R.id.textBudgetLabel).text =
                "Today's ${pair.category} Budget"
            itemView.findViewById<TextView>(R.id.textExpenseLabel).text =
                "Today's ${pair.category} Expense"
        }
    }
}

