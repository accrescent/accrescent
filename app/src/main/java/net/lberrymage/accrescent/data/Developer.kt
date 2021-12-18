package net.lberrymage.accrescent.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "developers",
    indices = [Index(value = ["username"], unique = true), Index(
        value = ["public_key"],
        unique = true,
    )]
)
data class Developer(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val username: String,
    @ColumnInfo(name = "public_key") val publicKey: String
)
