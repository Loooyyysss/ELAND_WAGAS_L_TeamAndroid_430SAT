package com.ecocp.financex.ui.cat_and_exp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ecocp.financex.R

class CategoryAdapter(
    private val categories: MutableList<String>,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.categoryName)
        val deleteBtn: ImageButton = view.findViewById(R.id.deleteCategoryBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val name = categories[position]
        holder.nameText.text = name
        holder.deleteBtn.setOnClickListener {
            onDeleteClick(name)
        }
    }

    override fun getItemCount(): Int = categories.size

    fun updateData(newList: List<String>) {
        categories.clear()
        categories.addAll(newList)
        notifyDataSetChanged()
    }
}
