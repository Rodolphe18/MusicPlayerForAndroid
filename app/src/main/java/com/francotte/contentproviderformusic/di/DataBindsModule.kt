package com.francotte.contentproviderformusic.di

import com.francotte.contentproviderformusic.data.UserDataRepository
import com.francotte.contentproviderformusic.data.UserPreferencesDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindsModule {

    @Binds
    @Singleton
    abstract fun bindsUserDataRepository(
        impl: UserPreferencesDataSource,
    ): UserDataRepository
}
