package com.bugbd.pdfocr.adapter
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bugbd.pdfocr.R
import com.bugbd.pdfocr.databinding.LanguageRowBinding
import com.bugbd.pdfocr.model.LanguageSupported

class LanguageAdapter(
    private val list: List<LanguageSupported>,
    val context: Context,
    private var selectedPosition: Int = 0, // default 0
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

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val item = list[position]
            holder.binding.txt.text = item.name
            holder.binding.root.setBackgroundResource(
                if (position == selectedPosition) R.drawable.selected_round_stroke_bg
                else R.drawable.stroke_round_bg
            )

            // Item click
            holder.itemView.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onItem(item)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    init {
        // Auto-call listener for default selected item
        if (list.isNotEmpty()) {
            onItem(list[selectedPosition])
        }
    }

    class ViewHolder(val binding: LanguageRowBinding) : RecyclerView.ViewHolder(binding.root)
}