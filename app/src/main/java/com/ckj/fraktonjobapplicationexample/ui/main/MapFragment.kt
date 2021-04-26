package com.ckj.fraktonjobapplicationexample.ui.main

import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import com.ckj.fraktonjobapplicationexample.MainActivity
import com.ckj.fraktonjobapplicationexample.R
import com.ckj.fraktonjobapplicationexample.SharedViewModel
import com.ckj.fraktonjobapplicationexample.base.fragment.BaseFragment
import com.ckj.fraktonjobapplicationexample.data.db.PlaceEntity
import com.ckj.fraktonjobapplicationexample.databinding.FragmentMainBinding
import com.ckj.fraktonjobapplicationexample.ui.favorite_places.FavoritePlacesBottomSheet
import com.ckj.fraktonjobapplicationexample.ui.login.LoginFragment
import com.ckj.fraktonjobapplicationexample.util.SharedPreferenceUtil
import com.ckj.fraktonjobapplicationexample.util.bitmapDescriptorFromVector
import com.ckj.fraktonjobapplicationexample.util.roundTo
import com.ckj.fraktonjobapplicationexample.util.showDialog
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import javax.inject.Inject


class MapFragment : BaseFragment(), OnMapReadyCallback {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val mapViewModel: MapViewModel by viewModels { viewModelFactory }
    private val sharedViewModel: SharedViewModel by viewModels({ activity as MainActivity },
        { viewModelFactory })

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private var googleMap: GoogleMap? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as MainActivity).mainComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBaseFunctions()
//        (requireActivity() as MainActivity).monitorLocationPermissions()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMainBinding.inflate(inflater, container, false)

        binding.navigateButton.setOnClickListener {
            val enabled = (requireActivity() as MainActivity).sharedPreferences.getBoolean(
                SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false
            )
            if (enabled) {
                mapViewModel.placesUi.value.firstOrNull { it.isCurrentLocation }.let {
                    if (it != null) {
                        centerMap(it)
                    }
                }
            } else {
                (requireActivity() as MainActivity).monitorLocationPermissions()
            }
        }

        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun initView() {
        binding.addMore.setOnClickListener {
            findNavController().navigate(R.id.addNewPlaceFragment)
        }

        binding.listButton.setOnClickListener {
            val sheet = FavoritePlacesBottomSheet{
                showPlaceDetails(it)
            }
            sheet.show(childFragmentManager,"FavoriteBottomSheet")
//            findNavController().navigate(R.id.favoritePlacesBottomSheet)
        }

        binding.logOutButton.setOnClickListener {
            requireContext().showDialog(
                title = "Log Out",
                body = "Do you want to continue logging out?",
                onPositiveButton = {
                    AuthUI.getInstance()
                        .signOut(requireContext())
                        .addOnCompleteListener {
                            sharedViewModel.onSignOut()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                requireContext(),
                                "Something went wrong",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                },
                onNegativeButton = {

                }
            )
        }

    }

    override fun observeViewModel() {
        observeAuthenticationState()
        observePlaces()
    }

    private fun observePlaces() {
        mapViewModel.placesUi.asLiveData().observe(viewLifecycleOwner) { it ->
            if (!it.isNullOrEmpty()) {
                addMarkers(it)
                val currentPlace = it.firstOrNull { it.isCurrentLocation }
                if (currentPlace != null) {
                    centerMap(currentPlace)
                }
            } else {
                (requireActivity() as MainActivity).monitorLocationPermissions()
            }
        }
    }

    private fun observeAuthenticationState() {

        sharedViewModel.authenticationState.observe(viewLifecycleOwner, { authenticationState ->
            when (authenticationState) {
                SharedViewModel.AuthenticationState.AUTHENTICATED -> {
                    Log.e(LoginFragment.TAG, "AUTHENTICATED")
                    val mapFragment =
                        childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                    mapFragment.getMapAsync(this)
                }
                else -> {
                    Log.e(LoginFragment.TAG, authenticationState.name)
                    findNavController().navigate(R.id.loginFragment)
                    googleMap = null
                }
            }
        })
    }

    override fun onMapReady(p0: GoogleMap?) {
        if (p0 != null) {
            googleMap = p0
            mapViewModel.onMapReady()
            googleMap?.uiSettings?.isMyLocationButtonEnabled = false

            googleMap?.setOnMarkerClickListener {
                showPlaceDetails(it.tag as PlaceEntity)
                return@setOnMarkerClickListener true
            }
            googleMap?.setOnMapClickListener {
                binding.locationSheet.visibility = View.GONE
            }

            googleMap?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(), R.raw.map_style
                )
            )
        }
    }

    private fun addMarkers(list: List<PlaceEntity>) {
        if(list.first().longitude != 0.0 && list.first().latitude != 0.0) {

        for (i in list) {
            val marker = i.latitude?.let {
                i.longitude?.let { it1 ->
                    LatLng(
                        it,
                        it1
                    )
                }
            }?.let {
                MarkerOptions()
                    .position(
                        it
                    )
                    .icon(requireActivity().bitmapDescriptorFromVector())
                    .title(i.name)
            }

            googleMap?.addMarker(
                marker
            )?.tag = i

        } } else {
            (requireActivity() as MainActivity)
        }
    }


    private fun centerMap(place: PlaceEntity) {
        try {
            googleMap?.uiSettings?.isMyLocationButtonEnabled = false
            showPlaceDetails(place)
            googleMap?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    place.latitude?.let {
                        place.longitude?.let { it1 ->
                            LatLng(
                                it,
                                it1
                            )
                        }
                    }, 13f
                )
            )


        } catch (e: Exception) {
            binding.root.let {
                Snackbar.make(
                    it,
                    "E pamundur për të marrë vendndodhjen tuaj",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun showPlaceDetails(place: PlaceEntity) {


        binding.locationSheet.visibility = View.VISIBLE
        binding.locationNameTv.text = place.name ?: "Current Location"
        binding.locationLatitude.text =
            getString(R.string.latitude, place.latitude?.roundTo(2).toString())
        binding.locationLongitude.text =
            getString(R.string.longitude, place.longitude?.roundTo(2).toString())

        googleMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                place.longitude?.let {
                    place.latitude?.let { it1 ->
                        LatLng(
                            it1,
                            it
                        )
                    }
                }, 13f
            )
        )
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).monitorLocationPermissions()
    }
}
