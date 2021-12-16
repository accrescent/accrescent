package net.lberrymage.accrescent.data

import javax.inject.Inject

class MetadataRepository @Inject constructor(private val metadataRemoteDataSource: MetadataRemoteDataSource) {
    suspend fun fetchLatestMetadata(): Metadata = metadataRemoteDataSource.fetchLatestMetadata()
}
