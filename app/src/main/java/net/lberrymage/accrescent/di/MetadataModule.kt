package net.lberrymage.accrescent.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import net.lberrymage.accrescent.data.MetadataFetcher
import net.lberrymage.accrescent.data.MetadataFetcherImpl

@Module
@InstallIn(ViewModelComponent::class)
abstract class MetadataModule {
    @Binds
    abstract fun bindMetadataFetcher(metadataFetcherImpl: MetadataFetcherImpl): MetadataFetcher
}