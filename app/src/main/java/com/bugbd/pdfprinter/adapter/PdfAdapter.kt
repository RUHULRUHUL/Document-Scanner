package com.bugbd.pdfprinter.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.request.ImageRequest
import com.bugbd.pdfprinter.R
import com.bugbd.pdfprinter.databinding.DocumentItemBinding
import com.bugbd.pdfprinter.helper.Constants
import com.bugbd.pdfprinter.helper.Utils
import com.bugbd.pdfprinter.helper.Utils.Companion.formatFileSize
import com.bugbd.pdfprinter.helper.Utils.Companion.getPdfFileSizeFromUri
import com.bugbd.pdfprinter.helper.Utils.Companion.loadPdfFirstPageFromUri
import com.bugbd.pdfprinter.helper.logD
import com.bugbd.pdfprinter.model.ScanFile
import com.rajat.pdfviewer.PdfViewerActivity
import com.rajat.pdfviewer.util.saveTo
import java.io.File

class PdfAdapter(
    val context: Context,
    private val shareEvent:(model: ScanFile) -> Unit
) : RecyclerView.Adapter<PdfAdapter.ViewHolder>() {
    private val list: MutableList<ScanFile> = mutableListOf()

    fun addItems(items:List<ScanFile>){
        list.clear()
        list.addAll(items)
        notifyDataSetChanged()
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
            holder.binding.icon.loadPdfFirstPageFromUri(list[position].fileUrl.toUri(), context)
            val sizeBytes = getPdfFileSizeFromUri(context, list[position].fileUrl.toUri())
            val readableSize = formatFileSize(sizeBytes)
            holder.binding.title.text = list[position].fileName
            holder.binding.timeAgoTxt.text = "${Utils.timeAgo(list[position].time)}    $readableSize"
            holder.binding.pageTxt.text = Constants.totalPdfCount.toString()

            holder.binding.mainLayout.setOnClickListener {
                val item = list[position]
                "pdf path: ${item.fileUrl}".logD()
                shareEvent(list[position])
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    class ViewHolder(val binding: DocumentItemBinding) : RecyclerView.ViewHolder(binding.root)
}