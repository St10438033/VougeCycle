package com.example.vougecycle

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vougecycle.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSend.setOnClickListener {
            val identifier = binding.etIdentifier.text.toString().trim()

            if (identifier.isEmpty()) {
                Toast.makeText(this, "Please enter your email, phone or username", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Reset link sent to $identifier", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}