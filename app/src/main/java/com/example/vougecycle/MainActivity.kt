package com.example.vougecycle

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Temporary test: fetch products
        lifecycleScope.launch {
            try {
                val products = Supabase.client.postgrest
                    .from("products")
                    .select()
                Log.d("Supabase", "Products fetched: ${products.data}")
            } catch (e: Exception) {
                Log.d("Supabase", "Products fetch failed: ${e.message}")
            }
        }

        // Route based on session (as before)
        lifecycleScope.launch {
            kotlinx.coroutines.delay(200)
            val currentUser = try {
                Supabase.client.auth.currentUserOrNull()
            } catch (e: Exception) { null }

            val dest = if (currentUser != null) HomeActivity::class.java else LoginActivity::class.java
            startActivity(Intent(this@MainActivity, dest))
            finish()
        }
    }
}