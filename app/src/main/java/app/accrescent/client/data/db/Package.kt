package app.accrescent.client.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "packages",
    foreignKeys = [ForeignKey(
        entity = App::class,
        parentColumns = ["id"],
        childColumns = ["app_id"]
    )],
    primaryKeys = ["app_id", "file"]
)
data class Package(
    @ColumnInfo(name = "app_id") val appId: String,
    val file: String,
    val hash: String
)
