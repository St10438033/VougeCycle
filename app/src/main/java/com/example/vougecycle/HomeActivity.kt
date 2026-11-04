package com.example.vougecycle

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.vougecycle.databinding.ActivityHomeBinding
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class HomeActivity : BaseActivity() {

    override val selectedTab: Tab = Tab.HOME

    private lateinit var binding: ActivityHomeBinding
    private lateinit var adapter: ProductAdapter
    private val visibleProducts = mutableListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBell.setOnClickListener {
            Toast.makeText(this, "Notifications coming soon", Toast.LENGTH_SHORT).show()
        }
        binding.etSearch.setOnClickListener {
            startActivity(Intent(this, ExploreActivity::class.java))
        }

        setupProductGrid()
        setupBottomNav()
        fetchProducts()
    }
    override fun onResume() {
        super.onResume()
        fetchProducts()
    }

    private fun setupProductGrid() {
        adapter = ProductAdapter(
            products = visibleProducts,
            onHeartClick = { product ->
                Toast.makeText(this, "Favourited: ${product.title}", Toast.LENGTH_SHORT).show()
            },
            onCardClick = { product ->
                val intent = Intent(this, ProductDetailActivity::class.java).apply {
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
        binding.rvProducts.layoutManager = GridLayoutManager(this, 2)
        binding.rvProducts.adapter = adapter
    }

    private fun fetchProducts() {
        lifecycleScope.launch {
            try {
                val result = Supabase.client.postgrest
                    .from("products")
                    .select()
                val products = result.decodeList<Product>()

                visibleProducts.clear()
                visibleProducts.addAll(products.sortedByDescending { it.id })
                adapter.notifyDataSetChanged()

            } catch (e: Exception) {
                Log.e("Supabase", "Failed to load products: ${e.message}", e)
                Toast.makeText(this@HomeActivity, "Failed to load products", Toast.LENGTH_SHORT).show()
            }
        }
    }
}