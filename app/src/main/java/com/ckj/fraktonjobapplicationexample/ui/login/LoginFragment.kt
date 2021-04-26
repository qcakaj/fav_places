package com.ckj.fraktonjobapplicationexample.ui.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.ckj.fraktonjobapplicationexample.MainActivity
import com.ckj.fraktonjobapplicationexample.R
import com.ckj.fraktonjobapplicationexample.SharedViewModel
import com.ckj.fraktonjobapplicationexample.base.fragment.BaseFragment
import com.ckj.fraktonjobapplicationexample.databinding.FragmentLoginBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class LoginFragment : BaseFragment() {

    companion object {
        const val TAG = "LoginFragment"
        const val SIGN_IN_RESULT_CODE = 1001
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val sharedViewModel: SharedViewModel by viewModels({ activity as MainActivity },
        { viewModelFactory })

    private var _binding: FragmentLoginBinding? = null

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!

    private lateinit var navController: NavController

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as MainActivity).mainComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finish()
        }
        initBaseFunctions()

    }

    private fun launchSignInFlow() {
        // Give users the option to sign in / register with their email or Google account. If users
        // choose to register with their email, they will need to create a password as well.
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent. We listen to the response of this activity with the
        // SIGN_IN_RESULT_CODE code.

        startActivityForResult(
            AuthUI
                .getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.Theme_FraktonJobApplicationExample)
                .setLogo(R.drawable.ic_logo_white)
                .build(), SIGN_IN_RESULT_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in user.
                Log.i(
                    TAG,
                    "Successfully signed in user " +
                            "${FirebaseAuth.getInstance().currentUser?.displayName}!"
                )
            } else {
                // Sign in failed. If response is null the user canceled the sign-in flow using
                // the back button. Otherwise check response.getError().getErrorCode() and handle
                // the error.
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")

            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun initView() {
        binding.continueButton.setOnClickListener {
            launchSignInFlow()
        }
    }

    override fun observeViewModel() {
        observeAuthenticationState()
    }

    private fun observeAuthenticationState() {

        sharedViewModel.authenticationState.observe(viewLifecycleOwner, { authenticationState ->
            when (authenticationState) {
                SharedViewModel.AuthenticationState.AUTHENTICATED -> {
                    (requireActivity() as MainActivity).monitorLocationPermissions()
                    navController.popBackStack()
                    Log.e(TAG, "AUTHENTICATED")
                }
                else -> {
                    Log.e(TAG, authenticationState.name)

                }
            }
        })
    }
}