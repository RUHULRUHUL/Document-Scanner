package com.bugbd.pdfprinter.bottom_sheet

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bugbd.pdfprinter.R
import com.bugbd.pdfprinter.adapter.PdfOptionsAdapter
import com.bugbd.pdfprinter.databinding.BottomSheetBinding
import com.bugbd.pdfprinter.model.PdfOption
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

        val options = listOf(
            PdfOption("Rename", R.drawable.ic_language),
            PdfOption("Edit", R.drawable.baseline_qr_code_scanner_24),
            PdfOption("Share", R.drawable.ic_menu),
            PdfOption("Print", R.drawable.ic_menu),
            PdfOption("Delete", R.drawable.ic_menu)
        )

        val adapter = PdfOptionsAdapter(options) { option ->
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

