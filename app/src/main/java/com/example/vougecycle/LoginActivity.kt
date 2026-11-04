package com.example.vougecycle

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.vougecycle.databinding.ActivityLoginBinding
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvForgot.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!email.contains("@")) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.btnLogin.isEnabled = false
            binding.btnLogin.text = "Logging in..."

            lifecycleScope.launch {
                try {
                    Supabase.client.auth.signInWith(Email) {
                        this.email = email
                        this.password = password
                    }

                    Toast.makeText(
                        this@LoginActivity,
                        "Welcome back!",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                    finish()

                } catch (e: Exception) {
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = "Log In"
                    Toast.makeText(
                        this@LoginActivity,
                        "Login failed: ${e.message ?: "check your email and password"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}