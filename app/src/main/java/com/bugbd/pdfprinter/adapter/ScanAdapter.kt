package com.bugbd.pdfprinter.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bugbd.pdfprinter.databinding.ScanItemHorizontalBinding
import com.bugbd.pdfprinter.model.ScanItem

class ScanAdapter(
    val items: List<ScanItem>,
    val context: Context,
    private val onClick:(model: ScanItem) -> Unit
) : RecyclerView.Adapter<ScanAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ScanItemHorizontalBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        try {
            val item = items[position]
            holder.binding.itemIcon.setImageResource(item.icon)
            holder.binding.itemLabel.text = item.title

            holder.binding.root.setOnClickListener {
                val item = items[position]
                onClick(item)
            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
    }

    class ViewHolder(val binding: ScanItemHorizontalBinding) : RecyclerView.ViewHolder(binding.root)
}