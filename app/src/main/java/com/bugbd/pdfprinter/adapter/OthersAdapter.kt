package com.bugbd.pdfprinter.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bugbd.pdfprinter.databinding.OthersItemRowBinding
import com.bugbd.qrcode.model.LanguageItem

class OthersAdapter(
    private val list: List<LanguageItem>,
    val context: Context,
    private val onItem: (item: Int) -> Unit
) : RecyclerView.Adapter<OthersAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            OthersItemRowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = try {
        holder.binding.txt.text = list[position].title
        holder.binding.root.setOnClickListener {
            onItem(position)
            notifyItemChanged(position)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }


    class ViewHolder(val binding: OthersItemRowBinding) : RecyclerView.ViewHolder(binding.root)
}