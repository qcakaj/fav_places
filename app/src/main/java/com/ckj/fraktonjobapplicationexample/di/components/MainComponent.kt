package com.ckj.fraktonjobapplicationexample.di.components

import com.ckj.fraktonjobapplicationexample.MainActivity
import com.ckj.fraktonjobapplicationexample.di.ActivityScope
import com.ckj.fraktonjobapplicationexample.ui.favorite_places.FavoritePlacesBottomSheet
import com.ckj.fraktonjobapplicationexample.ui.login.LoginFragment
import com.ckj.fraktonjobapplicationexample.ui.add_place.AddNewPlaceFragment
import com.ckj.fraktonjobapplicationexample.ui.main.MapFragment

import dagger.Subcomponent

// Definition of a Dagger subcomponent
@ActivityScope
@Subcomponent(modules = [MainViewModelModule::class])
interface MainComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(mainActivity: LoginFragment)
    fun inject(mapFragment: MapFragment)
    fun inject(addNewPlaceFragment: AddNewPlaceFragment)
    fun inject(favoritePlacesBottomSheet: FavoritePlacesBottomSheet)


    // Factory to create instances of RegistrationComponent
    @Subcomponent.Factory
    interface Factory {
        fun create(): MainComponent
    }
}