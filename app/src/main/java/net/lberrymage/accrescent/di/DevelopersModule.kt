package net.lberrymage.accrescent.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import net.lberrymage.accrescent.data.DevelopersFetcher
import net.lberrymage.accrescent.data.DevelopersFetcherImpl

@Module
@InstallIn(ViewModelComponent::class)
abstract class DevelopersModule {
    @Binds
    abstract fun bindDevelopersFetcher(developersFetcherImpl: DevelopersFetcherImpl): DevelopersFetcher
}