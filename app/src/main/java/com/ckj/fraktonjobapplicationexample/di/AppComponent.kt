package com.ckj.fraktonjobapplicationexample.di

import android.content.Context
import com.ckj.fraktonjobapplicationexample.FraktonExApplication
import com.ckj.fraktonjobapplicationexample.di.components.MainComponent
import com.ckj.fraktonjobapplicationexample.util.location.ForegroundOnlyLocationService
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import javax.inject.Singleton

// Scope annotation that the AppComponent uses
// Classes annotated with @Singleton will have a unique instance in this Component
@Singleton
// Definition of a Dagger component that adds info from the different modules to the graph
@Component(modules = [
    AppModuleBinds::class,
    ViewModelBuilderModule::class,
    NetworkModule::class,
    DatabaseModule::class,
    SubcomponentsModule::class
])
interface AppComponent {

    // Factory to create instances of the AppComponent
    @Component.Factory
    interface Factory {
        // With @BindsInstance, the Context passed in will be available in the graph
        fun create(@BindsInstance context: Context): AppComponent
    }

    fun mainComponent(): MainComponent.Factory

    fun inject(FraktonExApplication: FraktonExApplication)
    fun inject(FraktonExApplication: ForegroundOnlyLocationService)
}

// This module tells a Component which are its subcomponents
@Module(
        subcomponents = [
            MainComponent::class,
         ])
class SubcomponentsModule