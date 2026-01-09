package com.example.contentproviderformusic.di

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object PlayerServiceModule {

    @Provides
    @ServiceScoped
    fun provideExoPlayer(
        @ApplicationContext app: Context
    ): ExoPlayer = ExoPlayer.Builder(app)
        .build()
}