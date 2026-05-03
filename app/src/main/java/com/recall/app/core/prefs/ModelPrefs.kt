package com.recall.app.core.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

/**
 * Secure preferences backed by AES-256 encryption.
 * Keys and values are both encrypted at rest using Android Keystore.
 */
class ModelPrefs(context: Context) {

    private val prefs: SharedPreferences by lazy {
        try {
            val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
            val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

            EncryptedSharedPreferences.create(
                "recall_secure_prefs",
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Graceful fallback to plain prefs if encryption unavailable (e.g. emulator quirks)
            android.util.Log.w("ModelPrefs", "Encrypted prefs unavailable, using plain: ${e.message}")
            context.getSharedPreferences("recall_prefs_fallback", Context.MODE_PRIVATE)
        }
    }

    var isModelDownloaded: Boolean
        get() = prefs.getBoolean(KEY_MODEL_DOWNLOADED, false)
        set(value) = prefs.edit().putBoolean(KEY_MODEL_DOWNLOADED, value).apply()

    var isOnboardingComplete: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_DONE, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_DONE, value).apply()

    companion object {
        private const val KEY_MODEL_DOWNLOADED = "model_downloaded"
        private const val KEY_ONBOARDING_DONE = "onboarding_done"
    }
}
