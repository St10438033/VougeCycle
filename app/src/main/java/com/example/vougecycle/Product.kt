package com.example.vougecycle

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    @SerialName("id") val id: String = "",
    @SerialName("seller_id") val sellerId: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("description") val description: String? = null,
    @SerialName("price") val price: Double = 0.0,
    @SerialName("category") val category: String? = null,
    @SerialName("condition") val condition: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("is_sold") val isSold: Boolean = false
) {
    fun priceFormatted(): String = "R %,.2f".format(price)
}