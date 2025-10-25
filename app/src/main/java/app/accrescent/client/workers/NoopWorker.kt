// SPDX-FileCopyrightText: © 2025 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class NoopWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
) : Worker(context, workerParams) {
    override fun doWork(): Result {
        return Result.success()
    }
}
