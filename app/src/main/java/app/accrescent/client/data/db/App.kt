package app.accrescent.client.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apps")
data class App(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "min_version_code") val minVersionCode: Int,
)
