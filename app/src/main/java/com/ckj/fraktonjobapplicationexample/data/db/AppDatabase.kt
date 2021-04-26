package com.ckj.fraktonjobapplicationexample.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import javax.inject.Singleton

@Database(
    entities = [
      PlaceEntity::class
    ],
    version = 1,exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun placeDao(): PlaceDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: getDatabase(context).also { INSTANCE = it }
            }
        }


        @Singleton
        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "AppDatabase"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                return instance
            }
        }

    }
}