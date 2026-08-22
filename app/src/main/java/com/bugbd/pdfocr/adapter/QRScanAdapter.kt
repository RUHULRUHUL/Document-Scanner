package com.bugbd.pdfocr.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bugbd.pdfocr.ScanDetailsActivity
import com.bugbd.pdfocr.databinding.QrScanItemBinding
import com.bugbd.pdfocr.helper.Utils
import com.bugbd.pdfocr.model.ScanHistory

class QRScanAdapter(
    private val context: Context
) : ListAdapter<ScanHistory, QRScanAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            QrScanItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ViewHolder(val binding: QrScanItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ScanHistory) {
            binding.title.text = item.title
            binding.descriptionTxt.text = item.description
            binding.dateTimeTxt.text = Utils.timeAgo(item.time)

            binding.root.setOnClickListener {
                val intent = Intent(context, ScanDetailsActivity::class.java)
                intent.putExtra("scanned_text", item.description)
                context.startActivity(intent)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ScanHistory>() {
        override fun areItemsTheSame(oldItem: ScanHistory, newItem: ScanHistory): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ScanHistory, newItem: ScanHistory): Boolean {
            return oldItem == newItem
        }
    }
}

