// SPDX-FileCopyrightText: © 2025 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.ui.state

sealed class Progress {
    data object Indeterminate : Progress()
    data class Determinate(val progress: Float) : Progress()
}
