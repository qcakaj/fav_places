package com.ckj.fraktonjobapplicationexample

import android.app.Application
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.util.CoilUtils
import coil.util.DebugLogger
import com.ckj.fraktonjobapplicationexample.di.AppComponent
import com.ckj.fraktonjobapplicationexample.di.DaggerAppComponent
import com.google.firebase.BuildConfig
import okhttp3.OkHttpClient


class FraktonExApplication : Application() , ImageLoaderFactory {

    // Instance of the AppComponent that will be used by all the Activities in the project
    val appComponent: AppComponent by lazy {
        initializeComponent()
    }

    private fun initializeComponent() : AppComponent {
        // Creates an instance of AppComponent using its Factory constructor
        // We pass the applicationContext that will be used as Context in the graph
        return DaggerAppComponent.factory().create(applicationContext)    }


    // enabling the singleton use of the ImageLoader over the app
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
                .availableMemoryPercentage(0.25) // Use 25% of the application's available memory.
                .crossfade(true) // Show a short crossfade when loading images from network or disk.
                .okHttpClient {
                    // Lazily create the OkHttpClient that is used for network operations.
                    OkHttpClient.Builder()
                            .cache(CoilUtils.createDefaultCache(applicationContext))
                            .build()
                }
                .apply {
                    // Enable logging to the standard Android log if this is a debug build.
                    if (BuildConfig.DEBUG) {
                        logger(DebugLogger(Log.VERBOSE))
                    }
                }
                .build()
    }
}