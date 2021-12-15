package net.lberrymage.accrescent.data

interface MetadataFetcher {
    suspend fun fetchLatestMetadata(): Metadata
}