package com.example.vougecycle

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Order(
    @SerialName("id") val id: String,
    @SerialName("buyer_id") val buyerId: String,
    @SerialName("seller_id") val sellerId: String,
    @SerialName("product_id") val productId: String,
    @SerialName("price") val price: Double,
    @SerialName("status") val status: String,
    @SerialName("created_at") val createdAt: String? = null
)