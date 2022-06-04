package app.accrescent.client.data.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RenameColumn
import androidx.room.RenameTable
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec

@Database(
    autoMigrations = [AutoMigration(from = 1, to = 2, spec = AppDatabase.OneToTwo::class)],
    entities = [App::class, SigningCert::class],
    version = 2,
)
abstract class AppDatabase : RoomDatabase() {
    @RenameColumn(
        tableName = "signing_keys",
        fromColumnName = "public_key_hash",
        toColumnName = "cert_hash",
    )
    @RenameTable(fromTableName = "signing_keys", toTableName = "signing_certs")
    class OneToTwo : AutoMigrationSpec

    abstract fun appDao(): AppDao
    abstract fun signingCertDao(): SigningCertDao
}
