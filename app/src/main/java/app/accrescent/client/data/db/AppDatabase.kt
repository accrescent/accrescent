// SPDX-FileCopyrightText: © 2022 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.data.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RenameColumn
import androidx.room.RenameTable
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    autoMigrations = [
        AutoMigration(from = 1, to = 2, spec = AppDatabase.OneToTwo::class),
        AutoMigration(from = 2, to = 3, spec = AppDatabase.TwoToThree::class),
        AutoMigration(from = 3, to = 4, spec = AppDatabase.ThreeToFour::class),
    ],
    entities = [App::class],
    version = 5,
)
abstract class AppDatabase : RoomDatabase() {
    @RenameColumn(
        tableName = "signing_keys",
        fromColumnName = "public_key_hash",
        toColumnName = "cert_hash",
    )
    @RenameTable(fromTableName = "signing_keys", toTableName = "signing_certs")
    class OneToTwo : AutoMigrationSpec

    @DeleteColumn(tableName = "apps", columnName = "icon_hash")
    class TwoToThree : AutoMigrationSpec

    @DeleteColumn(tableName = "apps", columnName = "name")
    class ThreeToFour : AutoMigrationSpec

    abstract fun appDao(): AppDao

    companion object {
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create the apps table with the new schema
                db.execSQL(
                    """
                    CREATE TABLE apps_new (
                        id TEXT NOT NULL,
                        min_version_code INTEGER NOT NULL,
                        signing_cert_hash TEXT NOT NULL,
                        PRIMARY KEY(id)
                    )
                    """
                )

                // 2. Copy data from the old apps and signing_certs tables to the new apps table
                db.execSQL(
                    """
                    INSERT INTO apps_new (id, min_version_code, signing_cert_hash)
                    SELECT apps.id, apps.min_version_code, signing_certs.cert_hash
                    FROM apps
                    JOIN signing_certs ON signing_certs.app_id = apps.id
                    """
                )

                // 3. Drop the old apps and signing_certs tables
                db.execSQL("DROP TABLE apps")
                db.execSQL("DROP TABLE signing_certs")

                // 4. Rename the new apps table to replace the old apps table
                db.execSQL("ALTER TABLE apps_new RENAME TO apps")
            }
        }
    }
}
