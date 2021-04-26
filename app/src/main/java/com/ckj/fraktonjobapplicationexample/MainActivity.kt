package com.ckj.fraktonjobapplicationexample

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.ckj.fraktonjobapplicationexample.base.activity.BaseActivity
import com.ckj.fraktonjobapplicationexample.di.components.MainComponent
import com.ckj.fraktonjobapplicationexample.util.SharedPreferenceUtil
import com.ckj.fraktonjobapplicationexample.util.isLocationEnabled
import com.ckj.fraktonjobapplicationexample.util.location.ForegroundOnlyLocationService
import com.ckj.fraktonjobapplicationexample.util.showAppDoesNotWorkWithoutLocationAlertDialog
import com.google.android.gms.location.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import javax.inject.Inject


private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34


class MainActivity : BaseActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        const val TAG = "MainActivity"
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: SharedViewModel by viewModels { viewModelFactory }
    lateinit var mainComponent: MainComponent

    private var foregroundOnlyLocationServiceBound = false

    //     Provides location updates for while-in-use feature.
    private var foregroundOnlyLocationService: ForegroundOnlyLocationService? = null

    lateinit var sharedPreferences: SharedPreferences

    private lateinit var storage: FirebaseStorage
    lateinit var storageRef: StorageReference

    // Monitors connection to the while-in-use service.
    private val foregroundOnlyServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as ForegroundOnlyLocationService.LocalBinder
            foregroundOnlyLocationService = binder.service
            foregroundOnlyLocationService
            foregroundOnlyLocationServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            foregroundOnlyLocationService = null
            foregroundOnlyLocationServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        mainComponent = (application as FraktonExApplication).appComponent.mainComponent().create()
        mainComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        storage = Firebase.storage
        storageRef = storage.reference

        sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

    }

    fun monitorLocationPermissions() {
        if (isLocationEnabled()) {
            val enabled = sharedPreferences.getBoolean(
                SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false
            )

            if (enabled) {
                foregroundOnlyLocationService?.unsubscribeToLocationUpdates()
            } else {
                if (foregroundPermissionApproved()) {
                    foregroundOnlyLocationService?.subscribeToLocationUpdates()
                        ?: Log.d(TAG, "Service Not Bound")
                } else {
                    requestForegroundPermissions()
                }
            }
        } else {
            showAppDoesNotWorkWithoutLocationAlertDialog(false)
        }
    }

    override fun onStart() {
        super.onStart()

        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        val serviceIntent = Intent(this, ForegroundOnlyLocationService::class.java)
        bindService(serviceIntent, foregroundOnlyServiceConnection, Context.BIND_AUTO_CREATE)
    }


    override fun onStop() {
        if (foregroundOnlyLocationServiceBound) {
            unbindService(foregroundOnlyServiceConnection)
            foregroundOnlyLocationServiceBound = false
        }
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)

        super.onStop()
    }

    override fun observeViewModel() {
    }

    override fun initView() {
    }

    override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {
        if (p1 == SharedPreferenceUtil.KEY_FOREGROUND_ENABLED) {
            sharedPreferences.getBoolean(
                SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false
            )

        }
    }

    private fun foregroundPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun requestForegroundPermissions() {
        val provideRationale = foregroundPermissionApproved()


        if (provideRationale) {
            // Request permission
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            )
        } else {
            Log.d(TAG, "Request foreground only permission")
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            )
        }
    }


    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionResult")

        when (requestCode) {
            REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE -> when {
                grantResults.isEmpty() ->
                    Log.d(TAG, "User interaction was cancelled.")
                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                    // Permission was granted.
                    foregroundOnlyLocationService?.subscribeToLocationUpdates()
                else -> {
                    // Permission denied.
                    applicationDetailsIntent()
                }
            }
        }
    }

    private fun applicationDetailsIntent() {
        // Build intent that displays the App settings screen.
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts(
            "package",
            BuildConfig.APPLICATION_ID,
            null
        )
        intent.data = uri
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }


}