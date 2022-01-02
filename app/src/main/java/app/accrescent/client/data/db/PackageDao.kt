package app.accrescent.client.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PackageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackages(vararg packages: Package)

    @Query("SELECT * FROM packages WHERE app_id = :appId")
    suspend fun getForApp(appId: String): List<Package>

    @Query("DELETE FROM packages WHERE app_id = :appId AND file NOT IN (:filesToKeep)")
    suspend fun deleteRemoved(appId: String, filesToKeep: List<String>)
}
