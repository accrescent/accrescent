package app.accrescent.client.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE

@Entity(
    tableName = "signing_certs",
    primaryKeys = ["app_id", "cert_hash"],
    foreignKeys = [ForeignKey(
        entity = App::class,
        parentColumns = ["id"],
        childColumns = ["app_id"],
        onDelete = CASCADE,
    )],
)
data class SigningCert(
    @ColumnInfo(name = "app_id") val appId: String,
    @ColumnInfo(name = "cert_hash") val certHash: String,
)
