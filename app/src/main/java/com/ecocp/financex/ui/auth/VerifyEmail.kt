package com.ecocp.financex.ui.auth

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.widget.Toast
import com.ecocp.financex.R
import com.google.firebase.auth.FirebaseAuth

class VerifyEmail : AppCompatActivity() {

    private lateinit var resendEmailButton: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_verify_email)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        resendEmailButton = findViewById(R.id.resendEmailButton)

        resendEmailButton.setOnClickListener {
            val user = auth.currentUser
            if (user != null && !user.isEmailVerified) {
                user.sendEmailVerification()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Verification email sent!", Toast.LENGTH_SHORT).show()
                        } else {
                            val error = task.exception?.localizedMessage ?: "Unknown error"
                            Toast.makeText(this, "Failed to send email: $error", Toast.LENGTH_LONG).show()
                        }
                    }

            } else {
                Toast.makeText(this, "No user signed in or email already verified.", Toast.LENGTH_SHORT).show()
            }
        }

        val goBackButton = findViewById<Button>(R.id.gobackbutton)
        goBackButton.setOnClickListener {
            finish() // closes this activity
        }
    }
}
