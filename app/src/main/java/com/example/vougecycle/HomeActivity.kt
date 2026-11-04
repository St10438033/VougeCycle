package com.example.vougecycle

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.vougecycle.databinding.ActivityHomeBinding
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class HomeActivity : BaseActivity() {

    override val selectedTab: Tab = Tab.HOME

    private lateinit var binding: ActivityHomeBinding
    private lateinit var adapter: ProductAdapter
    private val visibleProducts = mutableListOf<Product>()
    private val favouriteIds = mutableSetOf<String>()

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
    }

    override fun onResume() {
        super.onResume()
        fetchProducts()
        fetchFavouriteIds()
    }

    private fun setupProductGrid() {
        adapter = ProductAdapter(
            products = visibleProducts,
            favouriteIds = emptySet(),
            onHeartClick = { product -> toggleFavourite(product) },
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
                val result = Supabase.client.postgrest.from("products").select()
                val products = result.decodeList<Product>()

                visibleProducts.clear()
                visibleProducts.addAll(products.sortedByDescending { it.id })
                adapter.notifyDataSetChanged()

            } catch (e: Exception) {
                Log.e("Supabase", "Failed to load products: ${e.message}", e)
            }
        }
    }

    private fun fetchFavouriteIds() {
        lifecycleScope.launch {
            try {
                val user = Supabase.client.auth.currentUserOrNull() ?: return@launch
                val favResult = Supabase.client.postgrest
                    .from("favourites")
                    .select { filter { eq("user_id", user.id) } }

                val favRows = favResult.decodeList<FavouriteRow>()
                favouriteIds.clear()
                favouriteIds.addAll(favRows.map { it.product_id })
                adapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Log.e("Supabase", "Failed to load favourites", e)
            }
        }
    }

    private fun toggleFavourite(product: Product) {
        val user = Supabase.client.auth.currentUserOrNull() ?: return
        val isCurrentlyFav = favouriteIds.contains(product.id)

        lifecycleScope.launch {
            try {
                if (isCurrentlyFav) {
                    Supabase.client.postgrest.from("favourites").delete {
                        filter {
                            eq("user_id", user.id)
                            eq("product_id", product.id)
                        }
                    }
                    favouriteIds.remove(product.id)
                } else {
                    val newRow = buildJsonObject {
                        put("user_id", JsonPrimitive(user.id))
                        put("product_id", JsonPrimitive(product.id))
                    }
                    Supabase.client.postgrest.from("favourites").insert(newRow)
                    favouriteIds.add(product.id)
                }
                adapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Toast.makeText(this@HomeActivity, "Failed to update favourite", Toast.LENGTH_SHORT).show()
            }
        }
    }
}