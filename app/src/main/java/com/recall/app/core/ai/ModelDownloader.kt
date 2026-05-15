package com.recall.app.core.ai

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.util.Log
import java.io.File

object ModelDownloader {

    private const val TAG = "ModelDownloader"

    // Embedding model is open and publicly accessible (no auth required)
    private const val EMBEDDING_MODEL_URL =
        "https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2/resolve/main/onnx/model.onnx"

    // Gemma LLM for MediaPipe — publicly mirrored on HuggingFace (litert-community)
    private const val LLM_MODEL_URL =
        "https://huggingface.co/litert-community/Gemma-2B-IT/resolve/main/gemma-2b-it-cpu-int4.bin"

    const val LLM_FILE_NAME = "llm_model.task"
    const val EMBEDDING_FILE_NAME = "embedding_model.onnx"

    // SharedPreferences key to track setup completion
    private const val PREFS_NAME = "recall_ai_prefs"
    private const val KEY_SETUP_COMPLETE = "ai_setup_complete"

    // ---- Status Checking ----

    fun isSetupComplete(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_SETUP_COMPLETE, false)
    }

    fun markSetupComplete(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SETUP_COMPLETE, true)
            .apply()
    }

    fun isEmbeddingModelReady(context: Context): Boolean {
        return getEmbeddingModelFile(context).exists()
    }

    fun isLlmModelReady(context: Context): Boolean {
        return getLlmModelFile(context).exists()
    }

    fun areBothModelsReady(context: Context): Boolean {
        return isEmbeddingModelReady(context) && isLlmModelReady(context)
    }

    // ---- Downloads ----

    /**
     * Starts the embedding model download via Android DownloadManager.
     * Returns the download ID so callers can track progress.
     */
    fun startEmbeddingDownload(context: Context): Long {
        val file = getEmbeddingModelFile(context)
        if (file.exists()) return -1L  // already downloaded

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(EMBEDDING_MODEL_URL))
            .setTitle("Recall — Downloading Search Engine")
            .setDescription("Semantic search model (~23 MB)")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, EMBEDDING_FILE_NAME)

        return dm.enqueue(request)
    }

    /**
     * Starts the LLM model download via Android DownloadManager.
     * Returns the download ID so callers can track progress.
     */
    fun startLlmDownload(context: Context): Long {
        val file = getLlmModelFile(context)
        if (file.exists()) return -1L  // already downloaded

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(LLM_MODEL_URL))
            .setTitle("Recall — Downloading AI Brain")
            .setDescription("Gemma language model (~1.5 GB)")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, LLM_FILE_NAME)
            .setAllowedOverMetered(false)  // Only over WiFi for 1.5GB
            .setAllowedOverRoaming(false)

        return dm.enqueue(request)
    }

    // ---- Progress Tracking ----

    data class DownloadProgress(
        val bytesDownloaded: Long,
        val totalBytes: Long,
        val status: Int
    ) {
        val percent: Int
            get() = if (totalBytes > 0) ((bytesDownloaded * 100) / totalBytes).toInt() else 0

        val isComplete: Boolean
            get() = status == DownloadManager.STATUS_SUCCESSFUL

        val isFailed: Boolean
            get() = status == DownloadManager.STATUS_FAILED

        val isRunning: Boolean
            get() = status == DownloadManager.STATUS_RUNNING

        val isPending: Boolean
            get() = status == DownloadManager.STATUS_PENDING
    }

    /**
     * Query DownloadManager for the progress of a specific download ID.
     */
    fun queryProgress(context: Context, downloadId: Long): DownloadProgress? {
        if (downloadId == -1L) return null

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor: Cursor? = dm.query(query)

        cursor?.use {
            if (it.moveToFirst()) {
                val bytesDownloaded = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                val totalBytes = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                val status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                return DownloadProgress(bytesDownloaded, totalBytes, status)
            }
        }
        return null
    }

    // ---- File Paths ----

    fun getLlmModelFile(context: Context): File {
        return File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), LLM_FILE_NAME)
    }

    fun getEmbeddingModelFile(context: Context): File {
        return File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), EMBEDDING_FILE_NAME)
    }

    // ---- Legacy compatibility ----
    @Deprecated("Use the new setup flow instead", ReplaceWith(""))
    fun downloadModelsIfMissing(context: Context) {
        // No-op: replaced by the guided setup in ModelSetupActivity
        Log.w(TAG, "downloadModelsIfMissing is deprecated. Use ModelSetupActivity instead.")
    }
}
