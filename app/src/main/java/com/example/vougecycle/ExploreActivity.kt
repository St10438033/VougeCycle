package com.example.vougecycle

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.vougecycle.databinding.ActivityExploreBinding
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class ExploreActivity : BaseActivity() {

    override val selectedTab: Tab = Tab.EXPLORE

    private lateinit var binding: ActivityExploreBinding
    private lateinit var adapter: ProductAdapter
    private val allProducts = mutableListOf<Product>()
    private val visibleProducts = mutableListOf<Product>()
    private val favouriteIds = mutableSetOf<String>()
    private var activeChip: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExploreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGrid()
        setupChips()
        setupSearch()
        setupBottomNav()
    }

    override fun onResume() {
        super.onResume()
        fetchProducts()
        fetchFavouriteIds()
    }

    private fun setupGrid() {
        adapter = ProductAdapter(
            products = visibleProducts,
            favouriteIds = favouriteIds,
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
        binding.rvExplore.layoutManager = GridLayoutManager(this, 2)
        binding.rvExplore.adapter = adapter
    }

    private fun setupChips() {
        highlightChip(binding.chipAll)
        val chips = listOf(
            binding.chipAll, binding.chipWomen, binding.chipMen,
            binding.chipVintage, binding.chipSports
        )
        chips.forEach { chip ->
            chip.setOnClickListener {
                highlightChip(chip)
                applyFilter(binding.etSearch.text.toString(), chip.text.toString())
            }
        }
    }

    private fun highlightChip(chip: TextView) {
        activeChip?.setBackgroundResource(R.drawable.bg_chip)
        activeChip?.setTextColor(getColor(R.color.vc_black))
        chip.setBackgroundResource(R.drawable.bg_button_primary)
        chip.setTextColor(getColor(R.color.vc_white))
        activeChip = chip
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val currentChip = activeChip?.text?.toString() ?: "All"
                applyFilter(s?.toString() ?: "", currentChip)
            }
        })
    }

    private fun applyFilter(query: String, chipText: String) {
        val q = query.trim().lowercase()
        visibleProducts.clear()
        visibleProducts.addAll(
            allProducts.filter { p ->
                val matchesQuery = q.isEmpty() || p.title.lowercase().contains(q)
                val matchesChip = chipText == "All" || p.category?.equals(chipText, ignoreCase = true) == true
                matchesQuery && matchesChip
            }
        )
        adapter.notifyDataSetChanged()
    }

    private fun fetchProducts() {
        lifecycleScope.launch {
            try {
                val result = Supabase.client.postgrest.from("products").select()
                val products = result.decodeList<Product>()

                allProducts.clear()
                allProducts.addAll(products)

                val currentChip = activeChip?.text?.toString() ?: "All"
                applyFilter(binding.etSearch.text.toString(), currentChip)

            } catch (e: Exception) {
                Log.e("Supabase", "Explore fetch failed: ${e.message}", e)
                Toast.makeText(this@ExploreActivity, "Failed to load products", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@ExploreActivity, "Failed to update favourite", Toast.LENGTH_SHORT).show()
            }
        }
    }
}