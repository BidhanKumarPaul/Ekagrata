package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        AppPreferenceEntity::class,
        GoalEntity::class,
        FocusSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class EkagrataDatabase : RoomDatabase() {
    abstract fun appPreferenceDao(): AppPreferenceDao
    abstract fun goalDao(): GoalDao
    abstract fun focusSessionDao(): FocusSessionDao

    companion object {
        @Volatile
        private var INSTANCE: EkagrataDatabase? = null

        fun getInstance(context: Context): EkagrataDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EkagrataDatabase::class.java,
                    "ekagrata.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
