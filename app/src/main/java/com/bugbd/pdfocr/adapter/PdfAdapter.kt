package com.bugbd.pdfocr.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
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
        // Filter out files that don't exist, are empty, or cause errors
        val filteredList = newList.filter { item ->
            try {
                val uri = item.fileUrl.toUri()
                val size = getPdfFileSizeFromUri(context, uri)
                size > 0
            } catch (e: Exception) {
                false
            }
        }

        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = list.size
            override fun getNewListSize() = filteredList.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                list[oldItemPosition].id == filteredList[newItemPosition].id

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                list[oldItemPosition] == filteredList[newItemPosition]
        })
        list.clear()
        list.addAll(filteredList)
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
            val item = list.getOrNull(position) ?: return
            if (item.fileName.isNotEmpty()) {
                val fileUri = try {
                    item.fileUrl.toUri()
                } catch (e: Exception) {
                    null
                }

                val sizeBytes = fileUri?.let { getPdfFileSizeFromUri(context, it) } ?: 0L
                val readableSize = if (sizeBytes > 0) "    ${formatFileSize(sizeBytes)}" else ""

                // Hide tag if size is 0 (file missing or empty)
                holder.binding.tag.visibility = if (sizeBytes > 0) View.VISIBLE else View.GONE
                holder.binding.title.text = item.fileName
                holder.binding.timeAgoTxt.text = "${Utils.timeAgo(item.time)}$readableSize"

                if (item.fileName.contains(".jpeg", ignoreCase = true) ||
                    item.fileName.contains(".jpg", ignoreCase = true) ||
                    item.fileName.contains(".png", ignoreCase = true)
                ) {
                    holder.binding.tag.text = "Img"
                    holder.binding.pageTxt.visibility = View.GONE
                    holder.binding.icon.load(item.fileUrl) {
                        placeholder(R.drawable.ic_text_file)
                        error(R.drawable.ic_text_file)
                        crossfade(true)
                    }
                } else {
                    holder.binding.tag.text = "PDF"
                    val cacheKey = item.fileUrl
                    val cachedBitmap = PdfBitmapCache.get(cacheKey)
                    
                    if (cachedBitmap != null) {
                        holder.binding.icon.setImageBitmap(cachedBitmap)
                        holder.binding.pageTxt.visibility = View.GONE
                    } else {
                        holder.binding.icon.setImageResource(R.drawable.ic_text_file)
                        holder.binding.pageTxt.visibility = View.GONE
                    }

                    fileUri?.let { uri ->
                        renderFirstPageFromPdfUri(context, uri) { bitmap, totalPages ->
                            if (bitmap != null && cachedBitmap == null) {
                                holder.binding.icon.setImageBitmap(bitmap)
                                PdfBitmapCache.put(cacheKey, bitmap)
                            }
                            
                            if (totalPages > 0) {
                                holder.binding.pageTxt.visibility = View.VISIBLE
                                holder.binding.pageTxt.text = totalPages.toString()
                            } else {
                                holder.binding.pageTxt.visibility = View.GONE
                            }
                        }
                    } ?: run {
                        holder.binding.icon.setImageResource(R.drawable.ic_text_file)
                        holder.binding.pageTxt.visibility = View.GONE
                    }
                }
                
                holder.binding.mainLayout.setOnClickListener {
                    val currentPosition = holder.bindingAdapterPosition
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        val currentItem = list[currentPosition]
                        "pdf path: ${currentItem.fileUrl}".logD()
                        shareEvent(currentItem)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    class ViewHolder(val binding: DocumentItemBinding) : RecyclerView.ViewHolder(binding.root)
}