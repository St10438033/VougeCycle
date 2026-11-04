package com.example.vougecycle

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.vougecycle.databinding.ActivityExploreBinding
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class FavouritesActivity : BaseActivity() {

    override val selectedTab: Tab = Tab.PROFILE

    private lateinit var binding: ActivityExploreBinding
    private lateinit var adapter: ProductAdapter
    private val visibleProducts = mutableListOf<Product>()
    private val favouriteIds = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Reuse the Explore layout, but hide the search bar and chips
        binding = ActivityExploreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etSearch.visibility = android.view.View.GONE
        (binding.chipAll.parent as android.view.View).visibility = android.view.View.GONE

        setupGrid()
        setupBottomNav()
        fetchFavourites()
    }

    private fun setupGrid() {
        adapter = ProductAdapter(
            products = visibleProducts,
            favouriteIds = favouriteIds,
            onHeartClick = { product -> toggleFavourite(product) },
            onCardClick = { product ->
                // Open detail activity
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
        binding.rvExplore.layoutManager = GridLayoutManager(this, 2)
        binding.rvExplore.adapter = adapter
    }

    private fun fetchFavourites() {
        lifecycleScope.launch {
            try {
                val user = Supabase.client.auth.currentUserOrNull()
                if (user == null) return@launch

                val favResult = Supabase.client.postgrest
                    .from("favourites")
                    .select { filter { eq("user_id", user.id) } }

                val favRows = favResult.decodeList<FavouriteRow>()
                favouriteIds.clear()
                favouriteIds.addAll(favRows.map { it.product_id })

                if (favouriteIds.isEmpty()) {
                    Toast.makeText(this@FavouritesActivity, "No favourites yet!", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val productsResult = Supabase.client.postgrest
                    .from("products")
                    .select { filter { isIn("id", favouriteIds.toList()) } }

                val products = productsResult.decodeList<Product>()
                visibleProducts.clear()
                visibleProducts.addAll(products)
                adapter.notifyDataSetChanged()

            } catch (e: Exception) {
                Log.e("Supabase", "Failed to load favourites", e)
                Toast.makeText(this@FavouritesActivity, "Failed to load favourites", Toast.LENGTH_SHORT).show()
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
                    visibleProducts.removeAll { it.id == product.id }
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
                Toast.makeText(this@FavouritesActivity, "Failed to update favourite", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@kotlinx.serialization.Serializable
data class FavouriteRow(
    val id: String,
    val user_id: String,
    val product_id: String
)