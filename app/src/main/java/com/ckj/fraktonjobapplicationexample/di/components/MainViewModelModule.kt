package com.ckj.fraktonjobapplicationexample.di.components

import androidx.lifecycle.ViewModel

import com.ckj.fraktonjobapplicationexample.SharedViewModel
import com.ckj.fraktonjobapplicationexample.di.ViewModelKey
import com.ckj.fraktonjobapplicationexample.ui.favorite_places.FavoritePlacesViewModel
import com.ckj.fraktonjobapplicationexample.ui.add_place.AddNewPlaceViewModel
import com.ckj.fraktonjobapplicationexample.ui.login.LoginViewModel
import com.ckj.fraktonjobapplicationexample.ui.main.MapViewModel

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class MainViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(SharedViewModel::class)
    abstract fun bindSharedViewModel(viewModel: SharedViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    abstract fun bindLoginViewModel(viewModel: LoginViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MapViewModel::class)
    abstract fun bindMapViewModel(viewModel: MapViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AddNewPlaceViewModel::class)
    abstract fun bindAddNewPlaceViewModel(viewModel: AddNewPlaceViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FavoritePlacesViewModel::class)
    abstract fun bindFavoritePlacesViewModel(viewModel: FavoritePlacesViewModel): ViewModel
}
