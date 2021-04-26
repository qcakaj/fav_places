package com.ckj.fraktonjobapplicationexample.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(images: List<PlaceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place: PlaceEntity) : Long

    @Query("SELECT * FROM places")
     fun getPlaces(): Flow<List<PlaceEntity>>

     @Query("SELECT * FROM places WHERE :id LIKE isCurrent")
     fun getCurrentPlace(id:Boolean?=true) : Flow<PlaceEntity>

    @Query("DELETE FROM places")
    suspend fun clearPlaces()

    @Query("DELETE FROM places WHERE :id LIKE isCurrent ")
    suspend fun clearCurrentPlace(id:Boolean?=true)


}