package app.accrescent.client.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [App::class, SigningKey::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
    abstract fun signingKeyDao(): SigningKeyDao
}
