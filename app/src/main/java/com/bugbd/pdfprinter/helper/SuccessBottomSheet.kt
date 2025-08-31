//package com.bugbd.qrcode.helper
//
//import android.app.Dialog
//import android.os.Bundle
//import android.view.LayoutInflater
//import androidx.appcompat.app.AlertDialog
//import com.bugbd.qrcode.R
//import com.bugbd.qrcode.databinding.SuccessBottomBinding
//import com.google.android.material.bottomsheet.BottomSheetDialog
//import com.google.android.material.bottomsheet.BottomSheetDialogFragment
//
//class SuccessBottomSheet : BottomSheetDialogFragment() {
//    private var _binding: SuccessBottomBinding? = null
//    private val binding get() = _binding!!
//
//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val dialog = BottomSheetDialog(requireActivity())
//        _binding = SuccessBottomBinding.inflate(LayoutInflater.from(context))
//        dialog.setContentView(binding.root)
//        dialog.setCanceledOnTouchOutside(false)
//
//        val message = arguments?.getString(ARG_SUCCESS_MESSAGE) ?: ""
//        binding.idTVtext.text = message // replace with your actual TextView
//
//        binding.idIVimage.setOnClickListener {
//            dismiss()
//        }
//
//        return dialog
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//
//    companion object {
//        private const val ARG_SUCCESS_MESSAGE = "arg_success_message"
//
//        fun newInstance(successMessage: String): SuccessBottomSheet {
//            val fragment = SuccessBottomSheet()
//            val args = Bundle()
//            args.putString(ARG_SUCCESS_MESSAGE, successMessage)
//            fragment.arguments = args
//            return fragment
//        }
//    }
//}
