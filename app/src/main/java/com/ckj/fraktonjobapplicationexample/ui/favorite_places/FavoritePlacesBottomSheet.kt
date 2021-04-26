package com.ckj.fraktonjobapplicationexample.ui.favorite_places

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.ckj.fraktonjobapplicationexample.MainActivity
import com.ckj.fraktonjobapplicationexample.R
import com.ckj.fraktonjobapplicationexample.data.db.PlaceEntity
import com.ckj.fraktonjobapplicationexample.databinding.FragmentFavoritePlacesBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

class FavoritePlacesBottomSheet(private val onPlaceClicked: (PlaceEntity) -> Unit) :
    BottomSheetDialogFragment() {
    private var binding: FragmentFavoritePlacesBinding? = null
    private var adapter: FavoritePlacesAdapter? = null

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: FavoritePlacesViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogThemeNoFloating)
        (requireActivity() as MainActivity).mainComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavoritePlacesBinding.inflate(inflater)

        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = FavoritePlacesAdapter {
            dismissAllowingStateLoss()
            onPlaceClicked(it)
        }
        binding?.favPlacesRv?.layoutManager = LinearLayoutManager(activity)
        binding?.favPlacesRv?.adapter = adapter
        dialog?.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheet = d.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            val displayMetrics = DisplayMetrics()
            requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
            val height = displayMetrics.heightPixels
            bottomSheet.minimumHeight = height
        }
        binding?.cancelButton?.setOnClickListener { dismissAllowingStateLoss() }

        viewModel.placesUi.asLiveData().observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                adapter?.submitList(it)

            }

        }
        binding?.searchEt?.addTextChangedListener(SearchTextWatcher())

    }

    inner class SearchTextWatcher : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {

            val filteredCountries =
                viewModel.placesUi.value.filter { place ->
                    place.name?.startsWith(
                        p0.toString(),
                        true
                    ) == true
                }
            adapter?.submitList(filteredCountries)
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
        binding = null
    }
}
