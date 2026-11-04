package com.example.vougecycle

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.vougecycle.databinding.ActivityShopProfileBinding
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class ShopProfileActivity : BaseActivity() {

    override val selectedTab = Tab.PROFILE

    private lateinit var binding: ActivityShopProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Back
        binding.ivBack.setOnClickListener {
            finish()
        }

        // Add items → Sell screen
        binding.btnAddItems.setOnClickListener {
            startActivity(Intent(this, SellActivity::class.java))
        }

        // Settings → Settings screen (to be added)
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Placeholder links
        binding.tvAddLinks.setOnClickListener {
            Toast.makeText(this, "Add links coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.tvAddDescription.setOnClickListener {
            Toast.makeText(this, "Add description coming soon", Toast.LENGTH_SHORT).show()
        }

        setupShopGrid()
        setupBottomNav()
    }

    private fun setupShopGrid() {
        lifecycleScope.launch {
            try {
                val result = Supabase.client.postgrest.from("products").select()
                val products = result.decodeList<Product>()

                val adapter = ProductAdapter(
                    products = products,
                    onHeartClick = { product ->
                        Toast.makeText(this@ShopProfileActivity, "Favourited: ${product.title}", Toast.LENGTH_SHORT).show()
                    },
                    onCardClick = { product ->
                        val intent = Intent(this@ShopProfileActivity, ProductDetailActivity::class.java).apply {
                            putExtra(ProductDetailActivity.EXTRA_ID, product.id)
                            putExtra(ProductDetailActivity.EXTRA_TITLE, product.title)
                            putExtra(ProductDetailActivity.EXTRA_PRICE, product.priceFormatted())
                            putExtra(ProductDetailActivity.EXTRA_IMAGE_URL, product.imageUrl)
                            putExtra(ProductDetailActivity.EXTRA_SOLD, product.isSold)
                            putExtra(ProductDetailActivity.EXTRA_DESCRIPTION, product.description)
                            putExtra(ProductDetailActivity.EXTRA_CONDITION, product.condition)
                        }
                        startActivity(intent)
                    }
                )
                binding.rvShopProducts.layoutManager = GridLayoutManager(this@ShopProfileActivity, 2)
                binding.rvShopProducts.adapter = adapter

            } catch (e: Exception) {
                Toast.makeText(this@ShopProfileActivity, "Failed to load shop", Toast.LENGTH_SHORT).show()
            }
        }
    }
}