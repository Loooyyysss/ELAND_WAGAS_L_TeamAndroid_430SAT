package com.ecocp.financex.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.ecocp.financex.R
import com.ecocp.financex.MainActivity
import com.ecocp.financex.ui.home.HomeFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val usernameField = findViewById<EditText>(R.id.loginusername)
        val passwordField = findViewById<EditText>(R.id.loginpassword)
        val loginButton = findViewById<Button>(R.id.loginuserbutton)
        val createAccountText = findViewById<TextView>(R.id.createnewaccounttextview)
        val forgotPasswordText = findViewById<TextView>(R.id.forgotpasswordtextview)

        loginButton.setOnClickListener {
            val input = usernameField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (input.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if input is an email
            if (android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
                signInWithEmail(input, password)
                startActivity(Intent(this, HomeFragment::class.java))
            } else {
                // Treat input as username: fetch email from Firestore
                db.collection("users")
                    .whereEqualTo("username", input)
                    .get()
                    .addOnSuccessListener { docs ->
                        if (!docs.isEmpty) {
                            val email = docs.documents[0].getString("email")
                            if (email != null) {
                                signInWithEmail(email, password)
                            } else {
                                Toast.makeText(this, "Email not found for that username", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, "Username does not exist", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Login failed: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        createAccountText.setOnClickListener {
            startActivity(Intent(this, Registration::class.java))
        }

        forgotPasswordText.setOnClickListener {
            startActivity(Intent(this, ForgotPassword::class.java))
        }
    }

    private fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}
