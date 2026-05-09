package com.lyra.aura.di

import android.content.Context
import com.lyra.aura.data.PreferencesDataStore
import com.lyra.aura.data.PresetsRepository
import com.lyra.aura.service.DiscordGateway
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDiscordGateway(): DiscordGateway = DiscordGateway()

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context,
    ): PreferencesDataStore = PreferencesDataStore(context)

    @Provides
    @Singleton
    fun providePresetsRepository(
        @ApplicationContext context: Context,
    ): PresetsRepository = PresetsRepository(context)
}
