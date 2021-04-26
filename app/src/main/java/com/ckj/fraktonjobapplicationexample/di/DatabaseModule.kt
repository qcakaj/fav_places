package com.ckj.fraktonjobapplicationexample.di

import android.content.Context

import com.ckj.fraktonjobapplicationexample.data.db.AppDatabase
import com.ckj.fraktonjobapplicationexample.data.db.PlaceDao

import javax.inject.Singleton

import dagger.Module
import dagger.Provides

@Module
class DatabaseModule {


    @Singleton
    @Provides
    fun provideDb(context:Context): AppDatabase = AppDatabase.getInstance(context)

    @Singleton
    @Provides
    fun providePlaceDao(db: AppDatabase): PlaceDao = db.placeDao()

}