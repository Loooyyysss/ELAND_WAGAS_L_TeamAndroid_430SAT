package com.ecocp.financex

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import android.content.Intent
import androidx.appcompat.app.AlertDialog

import com.ecocp.financex.ui.auth.Login

import com.ecocp.financex.ui.home.HomeFragment
import com.ecocp.financex.ui.prof_and_sett.ProfileFragment
import com.ecocp.financex.ui.prof_and_sett.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val homeFragment = HomeFragment()
    private val profileFragment = ProfileFragment()
    private val settingsFragment = SettingsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Show default fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, homeFragment)
            .commit()

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, homeFragment)
                        .commit()
                    true
                }
                R.id.profileFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, profileFragment)
                        .commit()
                    true
                }
                R.id.settingsFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, settingsFragment)
                        .commit()
                    true
                }
                R.id.logout -> {
                    showLogoutDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                val intent = Intent(this, Login::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
