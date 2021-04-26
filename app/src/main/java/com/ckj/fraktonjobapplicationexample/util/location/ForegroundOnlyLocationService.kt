package com.ckj.fraktonjobapplicationexample.util.location

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.ckj.fraktonjobapplicationexample.FraktonExApplication
import com.ckj.fraktonjobapplicationexample.data.MainRepository
import com.ckj.fraktonjobapplicationexample.data.db.PlaceEntity
import com.ckj.fraktonjobapplicationexample.data.model.mapToLocationModel
import com.ckj.fraktonjobapplicationexample.util.SharedPreferenceUtil
import com.google.android.gms.location.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class ForegroundOnlyLocationService : Service() {

    @Inject
    lateinit var mainRepository: MainRepository
    private var configurationChange = false

    private var serviceRunningInForeground = false

    private val localBinder = LocalBinder()

    private lateinit var notificationManager: NotificationManager


    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // LocationRequest - Requirements for the location updates
    private lateinit var locationRequest: LocationRequest

    // LocationCallback - Called when FusedLocationProviderClient has a new Location.
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        (application as FraktonExApplication).appComponent.inject(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(60)

            // Sets the fastest rate for active location updates.
            fastestInterval = TimeUnit.SECONDS.toMillis(30)

            // Sets the maximum time when batched location updates are delivered
            maxWaitTime = TimeUnit.MINUTES.toMillis(2)

            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                val location = locationResult.lastLocation.mapToLocationModel()
                Log.d(TAG, "location()")
             if(location != null) {
               if(location.latitude != 0.0 && location.longitude != 0.0)
                onCurrentPlaceReady(
                    PlaceEntity(
                        longitude = location.longitude,
                        latitude = location.latitude,
                        isCurrentLocation = true
                    )
                )
            } else {
                Log.e(TAG,location.toString())
            }
            }
        }
    }

    fun onCurrentPlaceReady(placeEntity: PlaceEntity) {
        Log.d(TAG, "onCurrentPlaceReady()")

        GlobalScope.launch {
            mainRepository.deleteCurrentPlace()
            mainRepository.insertPlace(placeEntity)
        }
    }


    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind()")

        stopForeground(true)
        serviceRunningInForeground = false
        configurationChange = false
        return localBinder
    }

    @SuppressLint("LongLogTag")
    override fun onRebind(intent: Intent) {
        Log.d(TAG, "onRebind()")
        stopForeground(true)
        serviceRunningInForeground = false
        configurationChange = false
        super.onRebind(intent)
    }

    @SuppressLint("LongLogTag")
    override fun onUnbind(intent: Intent): Boolean {
        Log.d(TAG, "onUnbind()")

        // NOTE: If this method is called due to a configuration change in MainActivity,
        // we do nothing.
        if (!configurationChange && SharedPreferenceUtil.getLocationTrackingPref(this)) {
            serviceRunningInForeground = true
        }

        // Ensures onRebind() is called if MainActivity (client) rebinds.
        return true
    }

    @SuppressLint("LongLogTag")
    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configurationChange = true
    }

    @SuppressLint("LongLogTag")
    fun subscribeToLocationUpdates() {
        Log.d(TAG, "subscribeToLocationUpdates()")

        SharedPreferenceUtil.saveLocationTrackingPref(this, true)

     //start the service
        startService(Intent(applicationContext, ForegroundOnlyLocationService::class.java))

        try {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper()
            )
        } catch (unlikely: SecurityException) {
            SharedPreferenceUtil.saveLocationTrackingPref(this, false)
            Log.e(TAG, "Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }

    @SuppressLint("LongLogTag")
    fun unsubscribeToLocationUpdates() {
        Log.d(TAG, "unsubscribeToLocationUpdates()")

        try {
            val removeTask = fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            removeTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Location Callback removed.")
                    stopSelf()
                } else {
                    Log.d(TAG, "Failed to remove Location Callback.")
                }
            }
            SharedPreferenceUtil.saveLocationTrackingPref(this, false)
        } catch (unlikely: SecurityException) {
            SharedPreferenceUtil.saveLocationTrackingPref(this, true)
            Log.e(TAG, "Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        internal val service: ForegroundOnlyLocationService
            get() = this@ForegroundOnlyLocationService

    }

    companion object {
        private const val TAG = "LocationService"
    }
}