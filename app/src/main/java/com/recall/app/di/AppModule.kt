package com.recall.app.di

import android.content.Context
import androidx.room.Room
import com.recall.app.core.ai.EmbeddingEngine
import com.recall.app.core.data.local.AppDatabase
import com.recall.app.core.data.local.NoteDao
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "recall-db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideNoteDao(db: AppDatabase): NoteDao = db.noteDao()

    @Provides
    @Singleton
    fun provideEmbeddingEngine(@ApplicationContext context: Context): EmbeddingEngine =
        EmbeddingEngine(context)
}
