package com.ckj.fraktonjobapplicationexample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.ckj.fraktonjobapplicationexample.data.MainRepository
import com.ckj.fraktonjobapplicationexample.util.FirebaseUserLiveData
import javax.inject.Inject


class SharedViewModel @Inject constructor(
    private val mainRepository: MainRepository,
) : ViewModel() {
    fun onSignOut() {
        mainRepository.clearDb()
    }

    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED
    }


    //we can handle other cases later as we need it. As for this case we are just gonna handle when querying user returns null or else
    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }
}