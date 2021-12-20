package net.lberrymage.accrescent.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "developers")
data class Developer(
    @PrimaryKey val username: String,
    @ColumnInfo(name = "public_key") val publicKey: String
)
