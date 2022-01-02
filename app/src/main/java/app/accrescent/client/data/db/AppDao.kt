package app.accrescent.client.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertApps(vararg apps: App)

    @Query("SELECT * from apps")
    fun getAll(): Flow<List<App>>

    @Query("SELECT version_code FROM apps WHERE id = :appId")
    suspend fun getVersion(appId: String): Int?

    @Update
    suspend fun updateApps(vararg apps: App)

    @Query("DELETE FROM apps WHERE id NOT IN (:idsToKeep)")
    suspend fun deleteRemoved(idsToKeep: List<String>)
}
