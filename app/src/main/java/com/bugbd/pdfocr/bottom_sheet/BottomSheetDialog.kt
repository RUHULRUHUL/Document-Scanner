package com.bugbd.pdfocr.bottom_sheet

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bugbd.pdfocr.R
import com.bugbd.pdfocr.adapter.PdfOptionsAdapter
import com.bugbd.pdfocr.databinding.BottomSheetBinding
import com.bugbd.pdfocr.model.PdfOption
import com.bugbd.pdfocr.model.pdfOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MyBottomSheetFragment(
    private val onOptionSelected: (String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetBinding.inflate(inflater, container, false)

        // RecyclerView setup
        binding.recyclerViewOptions.layoutManager = LinearLayoutManager(context)
        val items = mutableListOf<PdfOption>()
        items.clear()
        items.addAll(pdfOptions)

        val adapter = PdfOptionsAdapter(items) { option ->
            dismiss()
            onOptionSelected(option.title)
        }

        binding.recyclerViewOptions.adapter = adapter

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

