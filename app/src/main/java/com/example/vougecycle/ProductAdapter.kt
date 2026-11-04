package com.example.vougecycle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class ProductAdapter(
    private val products: List<Product>,
    // Pass in a set of product IDs that the user has favourited
    private val favouriteIds: Set<String>,
    private val onHeartClick: (Product) -> Unit = {},
    private val onCardClick: (Product) -> Unit = {}
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivProduct: ImageView = view.findViewById(R.id.ivProduct)
        val ivHeart: ImageView = view.findViewById(R.id.ivHeart)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val tvSoldBadge: TextView = view.findViewById(R.id.tvSoldBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]

        holder.tvTitle.text = product.title
        holder.tvPrice.text = product.priceFormatted()

        holder.ivProduct.load(product.imageUrl) {
            crossfade(true)
            placeholder(R.color.vc_grey_bg)
            error(R.color.vc_grey_bg)
        }

        holder.tvSoldBadge.visibility = if (product.isSold) View.VISIBLE else View.GONE

        // Set heart icon based on whether the ID is in our favourite set
        val isFav = favouriteIds.contains(product.id)
        holder.ivHeart.setImageResource(
            if (isFav) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
        )

        holder.ivHeart.setOnClickListener { onHeartClick(product) }
        holder.itemView.setOnClickListener { onCardClick(product) }
    }

    override fun getItemCount(): Int = products.size
}