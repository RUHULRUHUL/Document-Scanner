package com.bugbd.pdfprinter.adapter
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bugbd.pdfprinter.R
import com.bugbd.pdfprinter.databinding.LanguageRowBinding
import com.bugbd.qrcode.model.LanguageItem
import com.bugbd.qrcode.model.LanguageSupported

class LanguageAdapter(
    private val list: List<LanguageSupported>,
    val context: Context,
    private var selectedPosition: Int = -1,
    private val onItem: (item: LanguageSupported) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LanguageRowBinding.inflate(
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
        holder.binding.txt.text = list[position].name
        holder.binding.root.setBackgroundResource(
            if (position == selectedPosition) R.drawable.selected_round_stroke_bg
            else R.drawable.stroke_round_bg
        )
        holder.itemView.setOnClickListener {
            onItem(list[position])
            notifyItemChanged(selectedPosition)
            selectedPosition = position
            notifyItemChanged(selectedPosition)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    class ViewHolder(val binding: LanguageRowBinding) : RecyclerView.ViewHolder(binding.root)
}