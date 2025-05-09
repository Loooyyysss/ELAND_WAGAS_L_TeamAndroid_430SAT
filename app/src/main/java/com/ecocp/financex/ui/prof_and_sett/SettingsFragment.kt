package com.ecocp.financex.ui.prof_and_sett

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ecocp.financex.R
import com.ecocp.financex.db.DatabaseHelper
import com.ecocp.financex.ui.auth.Login
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsFragment : Fragment() {

    private lateinit var backButton: ImageButton
    private lateinit var privacyPolicyText: TextView
    private lateinit var logoutButton: Button
    private lateinit var deleteAccountButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize SQLite DatabaseHelper
        dbHelper = DatabaseHelper(requireContext())

        // Initialize views
        backButton = view.findViewById(R.id.settingbackbutton)
        privacyPolicyText = view.findViewById(R.id.privacy_policy_text)
        logoutButton = view.findViewById(R.id.logout_button)
        deleteAccountButton = view.findViewById(R.id.delete_account_button)

        // Set up back button
        backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        // Set up privacy policy click
        privacyPolicyText.setOnClickListener {
            showPrivacyPolicyDialog()
        }

        // Set up logout button
        logoutButton.setOnClickListener {
            showLogoutConfirmation()
        }

        // Set up delete account button
        deleteAccountButton.setOnClickListener {
            showDeleteAccountConfirmation()
        }
    }

    private fun showPrivacyPolicyDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Privacy Policy")
            .setMessage("This is a placeholder for the privacy policy. Please visit our website for the full policy.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                // Sign out from Firebase Auth
                auth.signOut()
                Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
                // Navigate to login screen
                startActivity(Intent(requireContext(), Login::class.java))
                requireActivity().finish()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    private fun showDeleteAccountConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Yes") { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    private fun deleteAccount() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "No user logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Step 1: Delete Firestore user data
        val userId = user.uid
        firestore.collection("users").document(userId)
            .delete()
            .addOnSuccessListener {
                // Step 2: Delete local SQLite data
                deleteLocalData()

                // Step 3: Delete Firebase Auth user
                user.delete()
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show()
                        // Navigate to login screen
                        startActivity(Intent(requireContext(), Login::class.java))
                        requireActivity().finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Failed to delete account: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to delete Firestore data: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun deleteLocalData() {
        // Drop all tables and recreate them to clear local data
        val db = dbHelper.writableDatabase
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS categories")
        db.execSQL("DROP TABLE IF EXISTS expenses")
        db.execSQL("DROP TABLE IF EXISTS budgets")
        dbHelper.onCreate(db)
        db.close()
    }
}