package net.lberrymage.accrescent.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.lberrymage.accrescent.data.RepoDataFetcher
import net.lberrymage.accrescent.data.RepoDataFetcherImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class RepoDataModule {
    @Binds
    abstract fun bindRepoDataFetcher(repoDataFetcherImpl: RepoDataFetcherImpl): RepoDataFetcher
}
