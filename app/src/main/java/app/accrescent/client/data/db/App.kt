package app.accrescent.client.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import app.accrescent.client.data.Developer

@Entity(
    tableName = "apps",
    foreignKeys = [ForeignKey(
        entity = Developer::class,
        parentColumns = ["username"],
        childColumns = ["maintainer"],
    )]
)
data class App(
    @PrimaryKey val id: String,
    @ColumnInfo(index = true) val maintainer: String,
    @ColumnInfo(name = "version_code") val versionCode: Int? = null,
)
