package com.recall.app.core.prefs

import android.content.Context
import android.content.SharedPreferences

/**
 * Lightweight SharedPreferences wrapper for persisting app-level flags.
 */
class ModelPrefs(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("recall_prefs", Context.MODE_PRIVATE)

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
