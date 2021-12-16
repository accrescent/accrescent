package net.lberrymage.accrescent.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MetadataRemoteDataSource @Inject constructor(private val metadataFetcher: MetadataFetcher) {
    suspend fun fetchLatestMetadata() =
        withContext(Dispatchers.IO) { metadataFetcher.fetchLatestMetadata() }
}
