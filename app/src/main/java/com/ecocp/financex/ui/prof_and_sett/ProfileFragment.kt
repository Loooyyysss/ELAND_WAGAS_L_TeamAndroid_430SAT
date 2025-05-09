package com.ecocp.financex.ui.prof_and_sett

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.ecocp.financex.R
import com.ecocp.financex.data.User
import com.ecocp.financex.db.DatabaseHelper
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var auth: FirebaseAuth
    private var currentUser: User? = null

    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var oldPasswordEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var backButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase and DB
        auth = FirebaseAuth.getInstance()
        dbHelper = DatabaseHelper(requireContext())

        // Get current user's email
        val email = auth.currentUser?.email
        if (email == null) {
            Toast.makeText(requireContext(), "No logged-in user", Toast.LENGTH_SHORT).show()
            return
        }

        // Fetch user from SQLite
        currentUser = dbHelper.getUserByEmail(email)
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not found in database", Toast.LENGTH_SHORT).show()
            return
        }

        // Bind views
        usernameEditText = view.findViewById(R.id.profileusername)
        emailEditText = view.findViewById(R.id.profileemail)
        oldPasswordEditText = view.findViewById(R.id.profileoldpassword)
        newPasswordEditText = view.findViewById(R.id.profilenewpassword)
        confirmPasswordEditText = view.findViewById(R.id.profileconfirmpassword)
        submitButton = view.findViewById(R.id.profilesubmitbutton)
        backButton = view.findViewById(R.id.profilebackbutton)

        // Set initial values
        usernameEditText.setText(currentUser!!.username)
        emailEditText.setText(currentUser!!.email)
        emailEditText.isEnabled = false // prevent changing email

        // Handle back button
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Handle submit button
        submitButton.setOnClickListener {
            updateProfile()
        }
    }

    private fun updateProfile() {
        val newUsername = usernameEditText.text.toString().trim()
        val oldPassword = oldPasswordEditText.text.toString().trim()
        val newPassword = newPasswordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        if (TextUtils.isEmpty(newUsername)) {
            usernameEditText.error = "Username required"
            return
        }

        if (TextUtils.isEmpty(oldPassword)) {
            oldPasswordEditText.error = "Enter current password"
            return
        }

        if (oldPassword != currentUser!!.password) {
            oldPasswordEditText.error = "Incorrect password"
            return
        }

        if (newPassword != confirmPassword) {
            confirmPasswordEditText.error = "Passwords do not match"
            return
        }

        val updatedUser = currentUser!!.copy(
            username = newUsername,
            password = if (newPassword.isNotEmpty()) newPassword else currentUser!!.password
        )

        if (dbHelper.updateUser(updatedUser)) {
            Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
            currentUser = updatedUser
            oldPasswordEditText.text.clear()
            newPasswordEditText.text.clear()
            confirmPasswordEditText.text.clear()
        } else {
            Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
        }
    }
}
