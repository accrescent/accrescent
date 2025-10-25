// SPDX-FileCopyrightText: © 2021 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.data.net

import kotlinx.serialization.Serializable

@Serializable
data class RepoData(
    val timestamp: Long,
    val apps: Map<String, App>,
)
