package com.francotte.contentproviderformusic.consent

import com.francotte.contentproviderformusic.consent.ConsentManager
import com.francotte.contentproviderformusic.consent.ConsentManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ConsentModule {

    @Binds
    @Singleton
    abstract fun bindConsentManager(impl: ConsentManagerImpl): ConsentManager
}
