package app.accrescent.client.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE

@Entity(
    tableName = "signing_keys",
    primaryKeys = ["app_id", "public_key_hash"],
    foreignKeys = [ForeignKey(
        entity = App::class,
        parentColumns = ["id"],
        childColumns = ["app_id"],
        onDelete = CASCADE,
    )],
)
data class SigningKey(
    @ColumnInfo(name = "app_id") val appId: String,
    @ColumnInfo(name = "public_key_hash") val publicKeyHash: String,
)
