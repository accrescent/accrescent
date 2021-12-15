package net.lberrymage.accrescent.data

class MetadataRepository(private val metadataRemoteDataSource: MetadataRemoteDataSource) {
    suspend fun fetchLatestMetadata(): Metadata = metadataRemoteDataSource.fetchLatestMetadata()
}
