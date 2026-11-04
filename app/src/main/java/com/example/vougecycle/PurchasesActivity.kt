package com.example.vougecycle

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.vougecycle.databinding.ActivityPurchasesBinding
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class PurchasesActivity : BaseActivity() {

    override val selectedTab: Tab = Tab.PROFILE

    private lateinit var binding: ActivityPurchasesBinding
    private lateinit var adapter: ProductAdapter
    private val purchasedProducts = mutableListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPurchasesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener { finish() }

        setupGrid()
        setupBottomNav()
        fetchPurchases()
    }

    private fun setupGrid() {
        adapter = ProductAdapter(
            products = purchasedProducts,
            favouriteIds = emptySet(),
            onHeartClick = { /* Not needed here for now */ },
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
        binding.rvPurchases.layoutManager = GridLayoutManager(this, 2)
        binding.rvPurchases.adapter = adapter
    }

    private fun fetchPurchases() {
        lifecycleScope.launch {
            try {
                val user = Supabase.client.auth.currentUserOrNull() ?: return@launch

                // 1. Get all orders where the buyer is the current user
                val orderResult = Supabase.client.postgrest
                    .from("orders")
                    .select { filter { eq("buyer_id", user.id) } }

                val orders = orderResult.decodeList<Order>()

                if (orders.isEmpty()) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.rvPurchases.visibility = View.GONE
                    return@launch
                }

                binding.tvEmptyState.visibility = View.GONE
                binding.rvPurchases.visibility = View.VISIBLE

                // 2. Extract all product IDs from the orders
                val productIds = orders.map { it.productId }

                // 3. Fetch the actual products
                val productResult = Supabase.client.postgrest
                    .from("products")
                    .select { filter { isIn("id", productIds) } }

                val products = productResult.decodeList<Product>()

                purchasedProducts.clear()
                purchasedProducts.addAll(products)
                adapter.notifyDataSetChanged()

            } catch (e: Exception) {
                Log.e("Supabase", "Failed to load purchases", e)
                Toast.makeText(this@PurchasesActivity, "Failed to load purchases", Toast.LENGTH_SHORT).show()
            }
        }
    }
}