// SPDX-FileCopyrightText: © 2025 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.ui.state

data class AppDetails(
    val appId: String,
    val name: String,
    val version: String,
    val shortDescription: String,
    val iconUrl: String,
)
