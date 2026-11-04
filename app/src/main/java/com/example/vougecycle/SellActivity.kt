package com.example.vougecycle

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.vougecycle.databinding.ActivitySellBinding
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import java.util.UUID
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject

class SellActivity : BaseActivity() {

    override val selectedTab: Tab = Tab.SELL

    private lateinit var binding: ActivitySellBinding

    // URI of the picked image (before upload)
    private var pickedImageUri: Uri? = null

    // Public URL after upload completes
    private var uploadedImageUrl: String? = null

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            pickedImageUri = uri
            binding.ivPreview.setImageURI(uri)
            binding.ivPreview.visibility = View.VISIBLE
            uploadImage(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySellBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Category spinner
        val categories = listOf("Choose category", "Women", "Men", "Era's", "Vintage", "Sports")
        binding.spCategory.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, categories
        )

        // Condition spinner
        val conditions = listOf(
            "Choose item condition",
            "New with tags", "New without tags",
            "Used - Excellent", "Used - Good", "Used - Fair"
        )
        binding.spCondition.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, conditions
        )

        binding.llUploadPhoto.setOnClickListener {
            pickImage.launch(
                androidx.activity.result.PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }

        binding.tvAdditional.setOnClickListener {
            Toast.makeText(this, "Additional Details coming soon", Toast.LENGTH_SHORT).show()
        }
        binding.tvDelivery.setOnClickListener {
            Toast.makeText(this, "Delivery options coming soon", Toast.LENGTH_SHORT).show()
        }

        // List Item
        binding.btnList.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val priceText = binding.etPrice.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()

            // Validation
            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (priceText.isEmpty()) {
                Toast.makeText(this, "Please enter a price", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val price = priceText.toDoubleOrNull()
            if (price == null || price <= 0) {
                Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (description.isEmpty()) {
                Toast.makeText(this, "Please add a description", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (binding.spCategory.selectedItemPosition == 0) {
                Toast.makeText(this, "Please choose a category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (binding.spCondition.selectedItemPosition == 0) {
                Toast.makeText(this, "Please choose a condition", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val category = binding.spCategory.selectedItem.toString()
            val condition = binding.spCondition.selectedItem.toString()

            // Logged-in user required
            val currentUser = Supabase.client.auth.currentUserOrNull()
            if (currentUser == null) {
                Toast.makeText(this, "You must be logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Disable button while inserting
            binding.btnList.isEnabled = false
            binding.btnList.text = "Listing..."

            lifecycleScope.launch {
                try {
                    // Build the row as JSON. `uploadedImageUrl` may be null — that's fine.
                    val newRow = buildJsonObject {
                        put("seller_id", JsonPrimitive(currentUser.id))
                        put("title", JsonPrimitive(title))
                        put("description", JsonPrimitive(description))
                        put("price", JsonPrimitive(price))
                        put("category", JsonPrimitive(category))
                        put("condition", JsonPrimitive(condition))
                        if (uploadedImageUrl != null) {
                            put("image_url", JsonPrimitive(uploadedImageUrl!!))
                        }
                        put("is_sold", JsonPrimitive(false))
                    }

                    Supabase.client.postgrest
                        .from("products")
                        .insert(newRow) {
                            select()
                        }

                    Toast.makeText(
                        this@SellActivity,
                        "Item listed successfully!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Return to previous screen (Home or Shop)
                    finish()

                } catch (e: Exception) {
                    binding.btnList.isEnabled = true
                    binding.btnList.text = "List Item"
                    Toast.makeText(
                        this@SellActivity,
                        "Listing failed: ${e.message ?: "unknown error"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        setupBottomNav()
    }

    private fun uploadImage(uri: Uri) {
        val currentUser = Supabase.client.auth.currentUserOrNull()
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.id
        val fileName = "${UUID.randomUUID()}.jpg"
        val path = "$userId/$fileName"

        binding.llUploadPhoto.isEnabled = false
        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                // 1. Copy the picked image to a cache file first (survives URI invalidation)
                val cacheFile = java.io.File(cacheDir, fileName)
                contentResolver.openInputStream(uri)?.use { input ->
                    cacheFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                } ?: throw IllegalStateException("Could not read selected image")

                // 2. Read the local copy into bytes
                val bytes = cacheFile.readBytes()

                // 3. Upload
                Supabase.client.storage
                    .from("product-images")
                    .upload(path, bytes) {
                        upsert = false
                    }

                // 4. Get the public URL
                val publicUrl = Supabase.client.storage
                    .from("product-images")
                    .publicUrl(path)

                uploadedImageUrl = publicUrl

                // 5. Clean up the cache file
                cacheFile.delete()

                Toast.makeText(
                    this@SellActivity,
                    "Photo uploaded",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {
                Toast.makeText(
                    this@SellActivity,
                    "Upload failed: ${e.message ?: "unknown error"}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.llUploadPhoto.isEnabled = true
            }
        }
    }
}