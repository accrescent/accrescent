package net.lberrymage.accrescent.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity(tableName = "developers")
@Serializable
data class Developer(
    @PrimaryKey val username: String,
    @ColumnInfo(name = "public_key") @SerialName("public_key") val publicKey: String
)
