// SPDX-FileCopyrightText: © 2021 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.di

import app.accrescent.client.data.RepoDataFetcher
import app.accrescent.client.data.RepoDataFetcherImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepoDataModule {
    @Binds
    abstract fun bindRepoDataFetcher(repoDataFetcherImpl: RepoDataFetcherImpl): RepoDataFetcher
}
