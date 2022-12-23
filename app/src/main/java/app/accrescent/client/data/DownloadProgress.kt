package app.accrescent.client.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class DownloadProgress(part: Long, total: Long) {
    var part by mutableStateOf(part)
    var total by mutableStateOf(total)
}
