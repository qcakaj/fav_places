package com.ckj.fraktonjobapplicationexample.ui.add_place

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.setPadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import coil.load
import coil.transform.RoundedCornersTransformation
import com.ckj.fraktonjobapplicationexample.MainActivity
import com.ckj.fraktonjobapplicationexample.SharedViewModel
import com.ckj.fraktonjobapplicationexample.base.fragment.BaseFragment
import com.ckj.fraktonjobapplicationexample.data.db.PlaceEntity
import com.ckj.fraktonjobapplicationexample.databinding.FragmentAddNewPlaceBinding
import com.ckj.fraktonjobapplicationexample.ui.main.ChooseImageBottomSheet
import com.ckj.fraktonjobapplicationexample.util.hideKeyboard
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class AddNewPlaceFragment : BaseFragment() {
    private lateinit var observer: MyLifecycleObserver

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: AddNewPlaceViewModel by viewModels { viewModelFactory }

    private var _binding: FragmentAddNewPlaceBinding? = null
    private val binding get() = _binding!!

    private var currentPhotoFile: Uri? = null


    private val permissions = listOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )


    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { resultMap ->

            resultMap.forEach {
                Log.e("sss", "Permission: ${it.key}, granted: ${it.value}")
            }

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observer = MyLifecycleObserver(requireActivity().activityResultRegistry, {
            viewModel.setImageUri(it.toString())
        }, {
            if (it) {
                viewModel.setImageUri(currentPhotoFile.toString())
//              binding.placeImageView.load(currentPhotoFile){
//
//              }
            }
        })
        lifecycle.addObserver(observer)
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as MainActivity).mainComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAddNewPlaceBinding.inflate(inflater, container, false)
        binding.topAppBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        requestPermissionsIfNecessary()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBaseFunctions()
    }

    override fun initView() {
        checkSaveButton()
        binding.placeImageView.setOnClickListener {

            val sheet = ChooseImageBottomSheet({
                observer.selectImage()
            }, {
                currentPhotoFile = getUri()
                currentPhotoFile?.let { it1 -> observer.takePicture(it1) }
            })
            sheet.show(childFragmentManager, "Sheet")
        }

        binding.savePlaceBtn.setOnClickListener {
            viewModel.onLoadingTask()
            uploadToFirebase()
        }

    }

    private fun uploadToFirebase() {
        val ref = getStorageRef()
        val uploadTask = viewModel.imageUri.value?.toUri()?.let { it1 -> ref.putFile(it1) }
        uploadTask?.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    viewModel.onUploadTaskError(it)

                }
            }
            ref.downloadUrl
        }?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                saveToDb(task)
            } else {
                task.exception?.let { it1 -> viewModel.onUploadTaskError(it1) }
            }
        }
    }

    private fun getStorageRef(): StorageReference {
        return (requireActivity() as MainActivity).storageRef.child(
            "images/".plus(
                UUID.randomUUID().toString()
            )
        )
    }

    private fun saveToDb(task: Task<Uri>) {
        viewModel.onUploadTaskSuccess(
            PlaceEntity(
                name = binding.nameOfPlaceEt.text.toString(),
                latitude = binding.latitudePlaceEt.text.toString().toDouble(),
                longitude = binding.longitudePlaceEt.text.toString().toDouble(),
                imageUri = task.result.toString(),
            )
        )
    }

    private fun getUri(): Uri? {
        var finalUri: Uri? = null
        val photoFile: File? = try {
            createImageFile().first
        } catch (ex: IOException) {
            // Error occurred while creating the File

            null
        }
        // Continue only if the File was successfully created
        photoFile?.also {
            finalUri = FileProvider.getUriForFile(
                requireContext(),
                "com.example.android.fileprovider",
                it
            )
        }
        return finalUri
    }

    @Throws(IOException::class)
    private fun createImageFile(): Pair<File, String> {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
        val path = file.absolutePath
        return Pair(file, path)
    }

    private fun checkSaveButton() {
        binding.nameOfPlaceEt.doAfterTextChanged {
            if (it?.isNotEmpty() == true) {
                enableContinueButton()
            } else {
                binding.savePlaceBtn.isEnabled = false
            }
        }
        binding.latitudePlaceEt.doAfterTextChanged {
            if (it?.isNotEmpty() == true) {
                enableContinueButton()
            } else {
                binding.savePlaceBtn.isEnabled = false
            }
        }
        binding.longitudePlaceEt.doAfterTextChanged {
            if (it?.isNotEmpty() == true) {
                enableContinueButton()
            } else {
                binding.savePlaceBtn.isEnabled = false
            }
        }

        if (viewModel.imageUri.value.toString().isNotBlank()) {
            enableContinueButton()
        } else {
            binding.savePlaceBtn.isEnabled = false
        }


    }


    override fun observeViewModel() {
        viewModel.imageUri.observe(viewLifecycleOwner) {
            if (!it.isNullOrBlank()) {
                binding.placeImageView.setPadding(0)
                binding.placeImageView.load(Uri.parse(it)) {
                    transformations(RoundedCornersTransformation(12f))
                }
                checkSaveButton()
            }
        }

        viewModel.placeStatusUiState.observe(viewLifecycleOwner, {
            when (it) {
                is PlaceStatusUiState.Error -> {
                    checkSaveButton()
                    Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT)
                        .show()

                }
                PlaceStatusUiState.Loading -> {
                    Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()
                    hideKeyboard()
                    binding.savePlaceBtn.isEnabled = false
                }
                is PlaceStatusUiState.Success -> {
                    Toast.makeText(requireContext(), "Place added successfully", Toast.LENGTH_SHORT)
                        .show()
                    findNavController().popBackStack()
                }
            }
        })
    }

    //enable if conditions are matched
    private fun enableContinueButton() {
        if (binding.nameOfPlaceEt.length() >= 3 &&
            binding.latitudePlaceEt.length() >= 3 &&
            binding.longitudePlaceEt.length() >= 3 &&
            viewModel.imageUri.value?.isNotBlank() == true

        ) {
            binding.savePlaceBtn.isEnabled = true

        }
    }


    private fun requestPermissionsIfNecessary() {
        if (!checkAllPermissions()) {
            requestPermissionsLauncher.launch(permissions.toTypedArray())

        } else {
            ///go to settings
        }
    }

    /** Permission Checking  */
    private fun checkAllPermissions(): Boolean {
        var hasPermissions = true
        for (permission in permissions) {
            hasPermissions = hasPermissions and isPermissionGranted(permission)
        }
        return hasPermissions
    }

    private fun isPermissionGranted(permission: String) =
        ActivityCompat.checkSelfPermission(requireContext(), permission) ==
                PackageManager.PERMISSION_GRANTED

}

