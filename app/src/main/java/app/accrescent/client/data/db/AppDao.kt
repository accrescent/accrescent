package app.accrescent.client.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertApps(vararg apps: App)

    @Query("SELECT * from apps")
    fun getAll(): Flow<List<App>>

    @Query("SELECT EXISTS (SELECT 1 FROM apps WHERE id = :appId)")
    suspend fun exists(appId: String): Boolean

    @Query("DELETE FROM apps WHERE id NOT IN (:idsToKeep)")
    suspend fun deleteRemoved(idsToKeep: List<String>)
}
