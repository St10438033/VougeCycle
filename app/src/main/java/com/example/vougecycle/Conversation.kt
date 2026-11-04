package com.example.vougecycle

data class Conversation(
    val sellerName: String,
    val lastMessage: String,
    val timeAgo: String,
    val unreadCount: Int = 0
)