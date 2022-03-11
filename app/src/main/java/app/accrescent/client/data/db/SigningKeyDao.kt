package app.accrescent.client.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SigningKeyDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSigningKeys(vararg signingKeys: SigningKey)

    @Query("SELECT public_key_hash FROM signing_keys WHERE app_id = :appId")
    fun getSignersForApp(appId: String): List<String>

    @Query("DELETE FROM signing_keys WHERE app_id = :appId AND public_key_hash NOT IN (:hashesToKeep)")
    suspend fun deleteRemoved(appId: String, hashesToKeep: List<String>)
}
