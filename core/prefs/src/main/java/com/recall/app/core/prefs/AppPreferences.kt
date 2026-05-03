package com.recall.app.core.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "recall_prefs")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val ONBOARDING_DONE  = booleanPreferencesKey("onboarding_done")
        val MODEL_DOWNLOADED = booleanPreferencesKey("model_downloaded")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
    }

    val isOnboardingComplete: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.ONBOARDING_DONE] ?: false }

    val isModelDownloaded: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.MODEL_DOWNLOADED] ?: false }

    val isBiometricEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.BIOMETRIC_ENABLED] ?: false }

    suspend fun setOnboardingComplete(done: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_DONE] = done }
    }

    suspend fun setModelDownloaded(downloaded: Boolean) {
        context.dataStore.edit { it[Keys.MODEL_DOWNLOADED] = downloaded }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BIOMETRIC_ENABLED] = enabled }
    }
}
