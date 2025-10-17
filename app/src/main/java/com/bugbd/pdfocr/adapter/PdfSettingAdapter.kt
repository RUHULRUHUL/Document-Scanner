package com.bugbd.pdfocr.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bugbd.pdfocr.databinding.PdfItemSettingItemBinding
import com.bugbd.pdfocr.model.PdfOption

class PdfOptionsAdapter(
    private val options: List<PdfOption>,
    private val onItemClick: (PdfOption) -> Unit
) : RecyclerView.Adapter<PdfOptionsAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: PdfItemSettingItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PdfItemSettingItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val option = options[position]
        holder.binding.title.text = option.title
        holder.binding.icon.setImageResource(option.iconRes)

        holder.binding.root.setOnClickListener {
            onItemClick(option)
        }
    }

    override fun getItemCount(): Int = options.size
}