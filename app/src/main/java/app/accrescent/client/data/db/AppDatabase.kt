package app.accrescent.client.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import app.accrescent.client.data.Developer

@Database(entities = [App::class, Developer::class, Package::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
    abstract fun developerDao(): DeveloperDao
    abstract fun packageDao(): PackageDao
}
