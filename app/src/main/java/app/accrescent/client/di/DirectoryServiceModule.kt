package app.accrescent.client.di

import android.content.Context
import app.accrescent.client.data.DIRECTORY_API_DOMAIN
import build.buf.gen.accrescent.directory.v1.DirectoryServiceGrpcKt
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.grpc.android.AndroidChannelBuilder
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DirectoryServiceModule {
    @Provides
    @Singleton
    fun provideDirectoryServiceStub(
        @ApplicationContext context: Context,
    ): DirectoryServiceGrpcKt.DirectoryServiceCoroutineStub {
        val channel = AndroidChannelBuilder
            .forTarget(DIRECTORY_API_DOMAIN)
            .context(context)
            .build()
        val stub = DirectoryServiceGrpcKt.DirectoryServiceCoroutineStub(channel)

        return stub
    }
}
