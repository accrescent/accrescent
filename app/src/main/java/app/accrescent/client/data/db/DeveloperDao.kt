package app.accrescent.client.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.accrescent.client.data.Developer
import kotlinx.coroutines.flow.Flow

@Dao
interface DeveloperDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevelopers(vararg developers: Developer)

    @Query("SELECT public_key FROM developers WHERE username = :username")
    fun getPublicKey(username: String): Flow<String?>

    @Query(
        """SELECT username, public_key FROM developers
        JOIN apps ON username = maintainer WHERE apps.id = :appId"""
    )
    suspend fun getMaintainer(appId: String): Developer?

    @Query("DELETE FROM developers WHERE username NOT IN (:usernamesToKeep)")
    suspend fun deleteRemoved(usernamesToKeep: List<String>)
}
