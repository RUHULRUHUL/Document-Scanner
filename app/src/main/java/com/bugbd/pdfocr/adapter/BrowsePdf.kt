package com.bugbd.pdfocr.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bugbd.pdfocr.databinding.DocumentItemBinding
import com.bugbd.pdfocr.helper.PdfFileItem


class BrowsePdf(
    val context: Context,
    private val items:List<PdfFileItem>?,
) : RecyclerView.Adapter<BrowsePdf.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DocumentItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return items?.size?:0
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        try {
            val item = items?.get(position)
            item?.let { item->
//                holder.binding.icon.loadPdfFirstPageFromUri(item.uri, context)
//                val sizeBytes = Utils.getPdfFileSizeFromUri(context, item.uri)
//                val readableSize = Utils.formatFileSize(sizeBytes)
                holder.binding.title.text = item.name
//                holder.binding.timeAgoTxt.text = "${Utils.timeAgo(item.createdTimeMillis)}    ${readableSize.toString()}"
//                holder.binding.pageTxt.text = item.pageCount.toString()
            }

//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
    }

    class ViewHolder(val binding: DocumentItemBinding) : RecyclerView.ViewHolder(binding.root)
}
