package com.recall.app.di

import android.content.Context
import com.recall.app.core.ai.EmbeddingEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AiEngineModule {

    @Provides
    @Singleton
    fun provideEmbeddingEngine(@ApplicationContext context: Context): EmbeddingEngine =
        EmbeddingEngine(context)
}
