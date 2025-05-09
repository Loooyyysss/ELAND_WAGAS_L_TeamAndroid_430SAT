package com.ecocp.financex.ui.cat_and_exp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ecocp.financex.R
import com.ecocp.financex.db.DatabaseHelper

class CategoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CategoryAdapter
    private lateinit var db: DatabaseHelper
    private lateinit var editText: EditText
    private lateinit var submitBtn: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_category, container, false)
        db = DatabaseHelper(requireContext())
        recyclerView = view.findViewById(R.id.CategoryRecyclerView)
        editText = view.findViewById(R.id.CategoryNameEditText)
        submitBtn = view.findViewById(R.id.categoriessubmitbutton)

        adapter = CategoryAdapter(mutableListOf()) { name ->
            db.deleteCategory(name)
            refreshList()
            Toast.makeText(requireContext(), "Deleted: $name", Toast.LENGTH_SHORT).show()
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        val backButton = view.findViewById<ImageView>(R.id.categorybackbutton)
        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        submitBtn.setOnClickListener {
            val name = editText.text.toString().trim()
            if (name.isNotEmpty()) {
                if (db.insertCategory(name)) {
                    editText.text.clear()
                    refreshList()
                } else {
                    Toast.makeText(requireContext(), "Category already exists!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        refreshList()
        return view
    }

    private fun refreshList() {
        adapter.updateData(db.getAllCategories())
    }
}
