package com.smarttv.webview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UrlAdapter(
    private val urls: List<String>,
    private val onUrlClick: (String) -> Unit
) : RecyclerView.Adapter<UrlAdapter.UrlViewHolder>() {

    class UrlViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.urlTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UrlViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_url, parent, false)
        return UrlViewHolder(view)
    }

    override fun onBindViewHolder(holder: UrlViewHolder, position: Int) {
        val url = urls[position]
        holder.textView.text = url
        holder.itemView.setOnClickListener {
            onUrlClick(url)
        }
        
        // Mejorar navegación con control remoto
        holder.itemView.isFocusable = true
        holder.itemView.isFocusableInTouchMode = true
    }

    override fun getItemCount() = urls.size
}
