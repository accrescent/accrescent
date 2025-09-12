package app.accrescent.client.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SigningCertDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSigningCerts(vararg signingCerts: SigningCert)

    @Query("SELECT cert_hash FROM signing_certs WHERE app_id = :appId")
    fun getSignersForApp(appId: String): List<String>
}
