// SPDX-FileCopyrightText: © 2025 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

package app.accrescent.client.di

import android.content.Context
import app.accrescent.client.data.APP_STORE_API_DOMAIN
import build.buf.gen.accrescent.appstore.v1.AppServiceGrpcKt
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.grpc.android.AndroidChannelBuilder
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppServiceModule {
    @Provides
    @Singleton
    fun provideAppServiceStub(
        @ApplicationContext context: Context,
    ): AppServiceGrpcKt.AppServiceCoroutineStub {
        val channel = AndroidChannelBuilder
            .forTarget(APP_STORE_API_DOMAIN)
            .context(context)
            .build()
        val stub = AppServiceGrpcKt.AppServiceCoroutineStub(channel)

        return stub
    }
}
