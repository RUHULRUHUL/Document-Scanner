package com.bugbd.pdfocr.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.bugbd.pdfocr.R
import com.bugbd.pdfocr.databinding.DocumentItemBinding
import com.bugbd.pdfocr.helper.PdfBitmapCache
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
                if (item.fileName.contains(".jpeg", ignoreCase = true) ||
                    item.fileName.contains(".jpg", ignoreCase = true) ||
                    item.fileName.contains(".png", ignoreCase = true)) {

                    holder.binding.icon.load(item.fileUrl) {
                        placeholder(R.drawable.ic_text_file) // optional
                        error(R.drawable.ic_text_file)             // optional
                        crossfade(true)                     // smooth fade animation
                    }
                    val sizeBytes = getPdfFileSizeFromUri(context, item.fileUrl.toUri())
                    val readableSize = formatFileSize(sizeBytes)
                    holder.binding.title.text = item.fileName
                    holder.binding.tag.text = "Img"
                    holder.binding.pageTxt.text = "1"
                    holder.binding.timeAgoTxt.text = "${Utils.timeAgo(item.time)}    $readableSize"
                }
                else{
                    val uri = item.fileUrl.toUri()
                    val cacheKey = item.fileUrl
                    val cachedBitmap = PdfBitmapCache.get(cacheKey)
                    if (cachedBitmap != null) {
                        holder.binding.icon.setImageBitmap(cachedBitmap)
                        holder.binding.pageTxt.text = "1"
                    } else {
                        renderFirstPageFromPdfUri(context, uri) { bitmap, totalPages ->
                            if (bitmap != null) {
                                holder.binding.icon.setImageBitmap(bitmap)
                                holder.binding.pageTxt.text = totalPages.toString()
                                PdfBitmapCache.put(cacheKey, bitmap)
                            }
                        }
                    }
                    val sizeBytes = getPdfFileSizeFromUri(context, uri)
                    val readableSize = formatFileSize(sizeBytes)
                    holder.binding.title.text = item.fileName
                    holder.binding.timeAgoTxt.text = "${Utils.timeAgo(item.time)}    $readableSize"
                }
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