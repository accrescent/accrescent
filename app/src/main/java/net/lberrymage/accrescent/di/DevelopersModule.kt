package net.lberrymage.accrescent.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.lberrymage.accrescent.data.DevelopersFetcher
import net.lberrymage.accrescent.data.DevelopersFetcherImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class DevelopersModule {
    @Binds
    abstract fun bindDevelopersFetcher(developersFetcherImpl: DevelopersFetcherImpl): DevelopersFetcher
}
