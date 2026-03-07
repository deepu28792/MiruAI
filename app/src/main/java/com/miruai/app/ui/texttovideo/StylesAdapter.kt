package com.miruai.app.ui.texttovideo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.miruai.app.R

class StylesAdapter(
    private val styles: List<String>,
    private var selectedStyle: String,
    private val onStyleSelected: (String) -> Unit
) : RecyclerView.Adapter<StylesAdapter.StyleViewHolder>() {

    inner class StyleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: CardView = itemView.findViewById(R.id.card_style)
        val tvStyle: TextView = itemView.findViewById(R.id.tv_style)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StyleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_style, parent, false)
        return StyleViewHolder(view)
    }

    override fun onBindViewHolder(holder: StyleViewHolder, position: Int) {
        val style = styles[position]
        holder.tvStyle.text = style

        val isSelected = style == selectedStyle
        val context = holder.itemView.context

        if (isSelected) {
            holder.card.setCardBackgroundColor(context.getColor(R.color.primary))
            holder.tvStyle.setTextColor(context.getColor(R.color.white))
        } else {
            holder.card.setCardBackgroundColor(context.getColor(R.color.bg_input))
            holder.tvStyle.setTextColor(context.getColor(R.color.text_secondary))
        }

        holder.itemView.setOnClickListener {
            selectedStyle = style
            notifyDataSetChanged()
            onStyleSelected(style)
        }
    }

    override fun getItemCount() = styles.size
}
