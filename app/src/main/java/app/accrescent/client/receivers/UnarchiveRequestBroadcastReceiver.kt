// SPDX-FileCopyrightText: © 2025 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import app.accrescent.client.data.appmanager.InstallWorkRepository
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

private const val UNARCHIVE_ID_UNSET = -999
private const val LOG_TAG = "UnarchiveRequestBroadcastReceiver"

@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
class UnarchiveRequestBroadcastReceiver : BroadcastReceiver() {
    @Inject
    lateinit var installWorkRepository: InstallWorkRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_UNARCHIVE_PACKAGE) {
            Log.w(LOG_TAG, "expected action UNARCHIVE_PACKAGE but got ${intent.action}, aborting")
            return
        }

        val packageName = intent
            .getStringExtra(PackageInstaller.EXTRA_UNARCHIVE_PACKAGE_NAME)
            ?: run {
                Log.e(LOG_TAG, "EXTRA_UNARCHIVE_PACKAGE_NAME expected but not set")
                return
            }
        val unarchiveId = intent
            .getIntExtra(PackageInstaller.EXTRA_UNARCHIVE_ID, UNARCHIVE_ID_UNSET)
        if (unarchiveId == UNARCHIVE_ID_UNSET) {
            Log.e(LOG_TAG, "EXTRA_UNARCHIVE_ID expected but not set")
            return
        }

        val unarchivalState = PackageInstaller.UnarchivalState.createOkState(unarchiveId)
        try {
            context.packageManager.packageInstaller.reportUnarchivalState(unarchivalState)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(LOG_TAG, "no unarchival with ID $unarchiveId exists", e)
            return
        }

        installWorkRepository.enqueueUnarchiveWorker(packageName, unarchiveId)
    }
}
