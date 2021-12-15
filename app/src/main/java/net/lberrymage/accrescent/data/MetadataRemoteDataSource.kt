package net.lberrymage.accrescent.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MetadataRemoteDataSource(private val metadataFetcher: MetadataFetcher) {
    suspend fun fetchLatestMetadata() =
        withContext(Dispatchers.IO) { metadataFetcher.fetchLatestMetadata() }
}
