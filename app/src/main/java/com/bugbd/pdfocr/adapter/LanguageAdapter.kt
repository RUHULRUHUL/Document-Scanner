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
            
            val isSelected = position == selectedPosition
            
            // Modern selection style
            holder.binding.root.strokeWidth = if (isSelected) 2 else 0
            holder.binding.root.cardElevation = if (isSelected) 4f else 0f
            
            val textColor = if (isSelected) {
                context.getColor(R.color.button_background)
            } else {
                context.getColor(R.color.textColor)
            }
            holder.binding.txt.setTextColor(textColor)
            holder.binding.icon.setColorFilter(textColor)

            // Item click
            holder.itemView.setOnClickListener {
                if (selectedPosition != position) {
                    val previousPosition = selectedPosition
                    selectedPosition = position
                    notifyItemChanged(previousPosition)
                    notifyItemChanged(selectedPosition)
                    onItem(item)
                }
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