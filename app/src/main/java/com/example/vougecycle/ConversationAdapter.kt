package com.example.vougecycle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ConversationAdapter(
    private val conversations: List<Conversation>,
    private val onRowClick: (Conversation) -> Unit = {}
) : RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder>() {

    inner class ConversationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAvatar: ImageView = view.findViewById(R.id.ivAvatar)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvLastMessage: TextView = view.findViewById(R.id.tvLastMessage)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvUnread: TextView = view.findViewById(R.id.tvUnread)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_conversation, parent, false)
        return ConversationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val convo = conversations[position]

        holder.tvName.text = convo.sellerName
        holder.tvLastMessage.text = convo.lastMessage
        holder.tvTime.text = convo.timeAgo

        if (convo.unreadCount > 0) {
            holder.tvUnread.visibility = View.VISIBLE
            holder.tvUnread.text = convo.unreadCount.toString()
        } else {
            holder.tvUnread.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onRowClick(convo) }
    }

    override fun getItemCount(): Int = conversations.size
}