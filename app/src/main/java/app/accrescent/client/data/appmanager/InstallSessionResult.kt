// SPDX-FileCopyrightText: © 2025 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.data.appmanager

sealed class InstallSessionResult {
    data object Success : InstallSessionResult()
    sealed class Failure(val message: String?) : InstallSessionResult() {
        class Generic(message: String?) : Failure(message)
        class Aborted(message: String?) : Failure(message)
        class Blocked(val blockingPackage: String?, message: String?) : Failure(message)
        class Conflict(val conflictingPackage: String?, message: String?) : Failure(message)
        class Incompatible(message: String?) : Failure(message)
        class Invalid(message: String?) : Failure(message)
        class Storage(val storagePath: String?, message: String?) : Failure(message)
        class Timeout(message: String?) : Failure(message)
    }

    data object PendingUserAction : InstallSessionResult()
}
