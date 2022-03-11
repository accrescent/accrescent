package app.accrescent.client.di

import android.content.Context
import androidx.room.Room
import app.accrescent.client.data.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, AppDatabase::class.java, "accrescent.db").build()

    @Provides
    fun provideAppDao(appDatabase: AppDatabase) = appDatabase.appDao()

    @Provides
    fun provideSigningKeyDao(appDatabase: AppDatabase) = appDatabase.signingKeyDao()
}
