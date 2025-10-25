// SPDX-FileCopyrightText: © 2025 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.InputStream
import java.io.OutputStream

fun InputStream.copyToWithProgress(
    out: OutputStream,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
): Flow<Long> {
    return flow {
        var bytesCopied: Long = 0
        val buffer = ByteArray(bufferSize)
        var bytes = read(buffer)
        while (bytes >= 0) {
            out.write(buffer, 0, bytes)
            bytesCopied += bytes
            emit(bytesCopied)
            bytes = read(buffer)
        }
    }.flowOn(Dispatchers.IO)
}
