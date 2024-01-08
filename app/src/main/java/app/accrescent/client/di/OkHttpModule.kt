package app.accrescent.client.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import javax.inject.Singleton

// The cache size in bytes
private const val CACHE_SIZE: Long = 50 * 1024 * 1024

@Module
@InstallIn(SingletonComponent::class)
object OkHttpModule {
    @Provides
    @Singleton
    fun getOkHttpClient(@ApplicationContext context: Context) = OkHttpClient()
        .newBuilder()
        .cache(
            Cache(
                directory = File(context.cacheDir.toURI()),
                maxSize = CACHE_SIZE,
            )
        )
        .build()
}
