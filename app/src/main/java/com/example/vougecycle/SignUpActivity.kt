package com.example.vougecycle

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.vougecycle.databinding.ActivitySignUpBinding
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.btnSignUp.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etUsername.text.toString().trim()   // ⚠️ we'll use this as the email field for now
            val password = binding.etPassword.text.toString().trim()
            val confirm = binding.etConfirmPassword.text.toString().trim()

            // --- Local validation (unchanged from before) ---
            if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                toast("Please fill in all fields")
                return@setOnClickListener
            }
            if (!email.contains("@")) {
                toast("Please enter a valid email in the Username field")
                return@setOnClickListener
            }
            if (password.length < 6) {
                toast("Password must be at least 6 characters")
                return@setOnClickListener
            }
            if (!password.any { it.isDigit() }) {
                toast("Password must contain a number")
                return@setOnClickListener
            }
            if (!password.any { it.isUpperCase() }) {
                toast("Password must contain a capital letter")
                return@setOnClickListener
            }
            if (password != confirm) {
                toast("Passwords do not match")
                return@setOnClickListener
            }

            // --- Call Supabase ---
            binding.btnSignUp.isEnabled = false
            binding.btnSignUp.text = "Creating account..."

            lifecycleScope.launch {
                try {
                    Supabase.client.auth.signUpWith(Email) {
                        this.email = email
                        this.password = password
                        // These get written into raw_user_meta_data,
                        // which our trigger reads to populate the profiles row
                        this.data = kotlinx.serialization.json.buildJsonObject {
                            put("username", kotlinx.serialization.json.JsonPrimitive(username))
                            put("full_name", kotlinx.serialization.json.JsonPrimitive(fullName))
                        }
                    }

                    toast("Account created! Welcome, $username")
                    startActivity(Intent(this@SignUpActivity, HomeActivity::class.java))
                    finish()

                } catch (e: Exception) {
                    binding.btnSignUp.isEnabled = true
                    binding.btnSignUp.text = "Sign Up"
                    toast("Sign up failed: ${e.message ?: "unknown error"}")
                }
            }
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}