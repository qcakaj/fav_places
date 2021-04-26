package com.ckj.fraktonjobapplicationexample.ui.add_place

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class MyLifecycleObserver(
    private val registry: ActivityResultRegistry,
    private val onUriReady: (Uri) -> Unit,
    private val onTakePicture: (Boolean) -> Unit,
) : DefaultLifecycleObserver {
    private lateinit var getContent: ActivityResultLauncher<String>
    private lateinit var takePicture: ActivityResultLauncher<Uri>


    override fun onCreate(owner: LifecycleOwner) {
        getContent = registry.register("getContent", owner,
            ActivityResultContracts.GetContent()
        ) { uri ->

            onUriReady(uri)

        }

        takePicture =
            registry.register("takePicture", owner, ActivityResultContracts.TakePicture()) {
                onTakePicture(it)
            }
    }

    fun selectImage() {
        getContent.launch("image/*")
    }

    fun takePicture(uri: Uri) {
    takePicture.launch(uri)
    }

}