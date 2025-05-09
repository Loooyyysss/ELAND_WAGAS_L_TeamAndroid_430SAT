package com.ecocp.financex.ui.auth

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ecocp.financex.R
import com.google.firebase.auth.FirebaseAuth

class ForgotPassword : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var sendEmailButton: Button
    private lateinit var goBackTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Initialize FirebaseAuth instance
        auth = FirebaseAuth.getInstance()

        // Find views by ID
        emailEditText = findViewById(R.id.forgotpasswordemail)
        sendEmailButton = findViewById(R.id.sendemailforgotpassbutton)
        goBackTextView = findViewById(R.id.gobacktextview)

        // Set up the "Send" button click listener
        sendEmailButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            } else {
                sendPasswordResetEmail(email)
            }
        }

        // Go back to login screen when the "Go back" link is clicked
        goBackTextView.setOnClickListener {
            finish()  // Closes the ForgotPassword screen and returns to the previous screen
        }
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Password reset email sent successfully
                    Toast.makeText(this, "Password reset email sent. Please check your inbox.", Toast.LENGTH_LONG).show()
                    finish()  // Close this activity and return to the login screen
                } else {
                    // If an error occurred, display the error message
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
