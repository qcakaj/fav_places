package com.ckj.fraktonjobapplicationexample.data

import com.ckj.fraktonjobapplicationexample.data.db.AppDatabase
import com.ckj.fraktonjobapplicationexample.data.db.PlaceDao
import com.ckj.fraktonjobapplicationexample.data.db.PlaceEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepository @Inject constructor(
    private val placeDao: PlaceDao,
    private val applicationDatabase: AppDatabase
) {

    val currentPlace: Flow<PlaceEntity> get() = placeDao.getCurrentPlace()
    val places: Flow<List<PlaceEntity>> get() = placeDao.getPlaces()


    suspend fun insertPlace(place: PlaceEntity): Long {
        return placeDao.insertPlace(place)
    }

    suspend fun deleteCurrentPlace() {
        placeDao.clearCurrentPlace()
    }

     fun clearDb() {
         GlobalScope.launch(Dispatchers.IO) {
             applicationDatabase.clearAllTables()
         }

    }


}