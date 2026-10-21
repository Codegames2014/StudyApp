package com.example.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [StudyVaultEntity::class, VaultItemEntity::class, MistakeEntity::class, StudentProfileEntity::class],
    version = 4,
    exportSchema = false
)
abstract class StudyFluidRoomDatabase : RoomDatabase() {

    abstract fun studyVaultDao(): StudyVaultDao
    abstract fun mistakeDao(): MistakeDao
    abstract fun studentProfileDao(): StudentProfileDao

    companion object {
        @Volatile
        private var INSTANCE: StudyFluidRoomDatabase? = null

        fun getDatabase(context: Context): StudyFluidRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudyFluidRoomDatabase::class.java,
                    "studyfluid_vaults.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
