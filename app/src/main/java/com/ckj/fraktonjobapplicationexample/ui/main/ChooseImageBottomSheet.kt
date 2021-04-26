package com.ckj.fraktonjobapplicationexample.ui.main

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import com.ckj.fraktonjobapplicationexample.R
import com.ckj.fraktonjobapplicationexample.databinding.ChooseImageLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ChooseImageBottomSheet(val onGalleryClicked: ()-> Unit, val onCameraClicked: ()-> Unit ) : BottomSheetDialogFragment() {


    private lateinit var binding: ChooseImageLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogThemeNoFloating)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ChooseImageLayoutBinding.inflate(inflater)
         binding.button1.setOnClickListener {
             dismissAllowingStateLoss()
             onGalleryClicked()
         }
        binding.deleteButton.setOnClickListener {
            dismissAllowingStateLoss()
            onCameraClicked()
        }
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog =
            super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { dialogInterface ->
            val d = dialogInterface as BottomSheetDialog
            val bottomSheet =
                d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
            bottomSheet?.let {
                BottomSheetBehavior.from<FrameLayout?>(bottomSheet).state =
                    BottomSheetBehavior.STATE_EXPANDED
            }

        }
        return dialog
    }


}