package com.bugbd.pdfocr.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bugbd.pdfocr.ScanDetailsActivity
import com.bugbd.pdfocr.databinding.QrScanItemBinding
import com.bugbd.pdfocr.helper.Utils
import com.bugbd.pdfocr.model.ScanHistory

class QRScanAdapter(
    val list: List<ScanHistory>,
    val context: Context
) : RecyclerView.Adapter<QRScanAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            QrScanItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            holder.binding.title.text = list[position].title
            holder.binding.descriptionTxt.text = list[position].description
            val scanTime = Utils.timeAgo(list[position].time)
            holder.binding.dateTimeTxt.text = scanTime

            holder.binding.mainLayout.setOnClickListener {
                val scanHistory = list[position]
                val intent = Intent(context, ScanDetailsActivity::class.java)
                intent.putExtra("scanned_text", scanHistory.description)
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    class ViewHolder(val binding: QrScanItemBinding) : RecyclerView.ViewHolder(binding.root)
}

