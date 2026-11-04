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

        // Load image (Coil handles null/URL gracefully)
        holder.ivProduct.load(product.imageUrl) {
            crossfade(true)
            placeholder(R.color.vc_grey_bg)
            error(R.color.vc_grey_bg)
        }

        // SOLD badge
        holder.tvSoldBadge.visibility = if (product.isSold) View.VISIBLE else View.GONE

        holder.ivHeart.setOnClickListener { onHeartClick(product) }
        holder.itemView.setOnClickListener { onCardClick(product) }
    }

    override fun getItemCount(): Int = products.size
}