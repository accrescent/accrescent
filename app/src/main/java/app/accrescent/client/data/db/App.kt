// SPDX-FileCopyrightText: © 2022 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apps")
data class App(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "min_version_code") val minVersionCode: Int,
    @ColumnInfo(name = "signing_cert_hash") val signingCertHash: String,
)
