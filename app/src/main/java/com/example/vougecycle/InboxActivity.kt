package com.example.vougecycle

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vougecycle.databinding.ActivityInboxBinding

class InboxActivity : BaseActivity() {

    override val selectedTab: Tab = Tab.INBOX

    private lateinit var binding: ActivityInboxBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInboxBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Demo conversations (replace with real data later)
        val conversations = listOf(
            Conversation(
                sellerName = "@vintage_kev",
                lastMessage = "Yes, still available! Want me to reserve it?",
                timeAgo = "2m",
                unreadCount = 2
            ),
            Conversation(
                sellerName = "@retro_hub",
                lastMessage = "Shipped — tracking number is on the way.",
                timeAgo = "1h",
                unreadCount = 0
            ),
            Conversation(
                sellerName = "@capetown_thrifts",
                lastMessage = "Thanks for the purchase! 🌿",
                timeAgo = "Yesterday",
                unreadCount = 0
            ),
            Conversation(
                sellerName = "@jhb_fits",
                lastMessage = "Can you do R1,400?",
                timeAgo = "Mon",
                unreadCount = 1
            )
        )

        // Show empty state if no conversations
        if (conversations.isEmpty()) {
            binding.rvConversations.visibility = View.GONE
            binding.llEmpty.visibility = View.VISIBLE
        } else {
            binding.rvConversations.visibility = View.VISIBLE
            binding.llEmpty.visibility = View.GONE

            val adapter = ConversationAdapter(conversations) { convo ->
                Toast.makeText(this, "Chat with ${convo.sellerName} coming soon", Toast.LENGTH_SHORT).show()
            }
            binding.rvConversations.layoutManager = LinearLayoutManager(this)
            binding.rvConversations.adapter = adapter
        }

        // Search icon → placeholder
        binding.ivSearch.setOnClickListener {
            Toast.makeText(this, "Inbox search coming soon", Toast.LENGTH_SHORT).show()
        }

        setupBottomNav()
    }
}