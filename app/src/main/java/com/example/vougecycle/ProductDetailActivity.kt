package com.example.vougecycle

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.vougecycle.databinding.ActivityProductDetailBinding
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class ProductDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "extra_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_PRICE = "extra_price"
        const val EXTRA_IMAGE_URL = "extra_image_url"
        const val EXTRA_SOLD = "extra_sold"
        const val EXTRA_DESCRIPTION = "extra_description"
        const val EXTRA_CONDITION = "extra_condition"
    }

    private lateinit var binding: ActivityProductDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val productId = intent.getStringExtra(EXTRA_ID) ?: ""
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Product"
        val price = intent.getStringExtra(EXTRA_PRICE) ?: "R 0.00"
        val imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL)
        val sold = intent.getBooleanExtra(EXTRA_SOLD, false)
        val description = intent.getStringExtra(EXTRA_DESCRIPTION) ?: "No description provided."
        val condition = intent.getStringExtra(EXTRA_CONDITION) ?: "Used"

        binding.tvTitle.text = title
        binding.tvPrice.text = price
        binding.tvCondition.text = if (sold) "SOLD" else condition
        binding.tvDescription.text = description
        binding.tvSellerName.text = "@shop_name"

        binding.ivProduct.load(imageUrl) {
            crossfade(true)
            placeholder(R.color.vc_grey_bg)
            error(R.color.vc_grey_bg)
        }

        binding.ivBack.setOnClickListener { finish() }

        var fav = false
        binding.ivHeart.setOnClickListener {
            fav = !fav
            binding.ivHeart.setImageResource(
                if (fav) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
            )
            Toast.makeText(
                this,
                if (fav) "Added to favourites" else "Removed from favourites",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.llSeller.setOnClickListener {
            startActivity(Intent(this, ShopProfileActivity::class.java))
        }

        binding.btnMessage.setOnClickListener {
            Toast.makeText(this, "Messaging coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.btnBuy.setOnClickListener {
            if (sold) {
                Toast.makeText(this, "This item has already been sold", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = Supabase.client.auth.currentUserOrNull()
            if (user == null) {
                Toast.makeText(this, "You must be logged in to buy", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val priceDouble = price.replace("R ", "").replace(",", "").toDoubleOrNull() ?: 0.0

            binding.btnBuy.isEnabled = false
            binding.btnBuy.text = "Processing..."

            lifecycleScope.launch {
                try {
                    // 1. Fetch the product to get the seller_id
                    val productRow = Supabase.client.postgrest
                        .from("products")
                        .select { filter { eq("id", productId) } }
                        .decodeSingle<Product>()

                    // 2. Insert the order
                    val newOrder = buildJsonObject {
                        put("buyer_id", JsonPrimitive(user.id))
                        put("seller_id", JsonPrimitive(productRow.sellerId))
                        put("product_id", JsonPrimitive(productId))
                        put("price", JsonPrimitive(priceDouble))
                        put("status", JsonPrimitive("pending"))
                    }

                    Supabase.client.postgrest.from("orders").insert(newOrder)

                    // 3. Mark product as sold
                    val updateProduct = buildJsonObject { put("is_sold", JsonPrimitive(true)) }
                    Supabase.client.postgrest.from("products").update(updateProduct) {
                        filter { eq("id", productId) }
                    }

                    Toast.makeText(this@ProductDetailActivity, "Order placed successfully!", Toast.LENGTH_LONG).show()
                    finish()

                } catch (e: Exception) {
                    binding.btnBuy.isEnabled = true
                    binding.btnBuy.text = "Buy now"
                    Toast.makeText(this@ProductDetailActivity, "Failed to buy: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}