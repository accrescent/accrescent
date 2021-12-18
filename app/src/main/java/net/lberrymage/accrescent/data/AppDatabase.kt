package net.lberrymage.accrescent.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Developer::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun developerDao(): DeveloperDao
}
