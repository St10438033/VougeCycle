package com.example.vougecycle

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.vougecycle.databinding.ActivitySettingsBinding
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener { finish() }

        binding.rowProfile.setOnClickListener {
            Toast.makeText(this, "Profile settings coming soon", Toast.LENGTH_SHORT).show()
        }
        binding.rowBank.setOnClickListener {
            Toast.makeText(this, "Bank account coming soon", Toast.LENGTH_SHORT).show()
        }
        binding.rowContact.setOnClickListener {
            Toast.makeText(this, "Contact details coming soon", Toast.LENGTH_SHORT).show()
        }
        binding.rowNotifications.setOnClickListener {
            Toast.makeText(this, "Notification settings coming soon", Toast.LENGTH_SHORT).show()
        }
        binding.rowPrivacy.setOnClickListener {
            Toast.makeText(this, "Privacy & consent coming soon", Toast.LENGTH_SHORT).show()
        }
        binding.rowAbout.setOnClickListener {
            Toast.makeText(this, "VougeCycle Pty Ltd", Toast.LENGTH_SHORT).show()
        }

        // Log out with confirmation dialog
        binding.rowLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Log out")
                .setMessage("Are you sure you want to log out?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Log out") { _, _ ->
                    lifecycleScope.launch {
                        try {
                            Supabase.client.auth.signOut()
                        } catch (e: Exception) {
                            // Even if signOut fails, we still want to send the user to Login
                        }
                        val intent = Intent(this@SettingsActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                }
                .show()
        }
    }
}