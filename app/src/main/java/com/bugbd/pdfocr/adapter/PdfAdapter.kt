package com.bugbd.pdfocr.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bugbd.pdfocr.databinding.DocumentItemBinding
import com.bugbd.pdfocr.helper.Utils
import com.bugbd.pdfocr.helper.Utils.Companion.formatFileSize
import com.bugbd.pdfocr.helper.Utils.Companion.getPdfFileSizeFromUri
import com.bugbd.pdfocr.helper.Utils.Companion.renderFirstPageFromPdfUri
import com.bugbd.pdfocr.helper.logD
import com.bugbd.pdfocr.model.ScanFile
import kotlinx.coroutines.MainScope

class PdfAdapter(
    val context: Context,
    private val shareEvent:(model: ScanFile) -> Unit
) : RecyclerView.Adapter<PdfAdapter.ViewHolder>() {
    private val list: MutableList<ScanFile> = mutableListOf()
    private val scope = MainScope()

    fun updateItems(newList: List<ScanFile>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = list.size
            override fun getNewListSize() = newList.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                list[oldItemPosition].id == newList[newItemPosition].id

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                list[oldItemPosition] == newList[newItemPosition]
        })
        list.clear()
        list.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

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
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val item = list[position]
            if (item.fileName.isNotEmpty()){
                renderFirstPageFromPdfUri(context, item.fileUrl.toUri()) { bitmap, totalPages ->
                    if (bitmap != null) {
                        holder.binding.icon.setImageBitmap(bitmap)
                        holder.binding.pageTxt.text = totalPages.toString()
                    }
                }
                val sizeBytes = getPdfFileSizeFromUri(context, list[position].fileUrl.toUri())
                val readableSize = formatFileSize(sizeBytes)
                holder.binding.title.text = list[position].fileName
                holder.binding.timeAgoTxt.text = "${Utils.timeAgo(list[position].time)}    $readableSize"
                holder.binding.mainLayout.setOnClickListener {
                    val item = list[position]
                    "pdf path: ${item.fileUrl}".logD()
                    shareEvent(list[position])
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    class ViewHolder(val binding: DocumentItemBinding) : RecyclerView.ViewHolder(binding.root)
}