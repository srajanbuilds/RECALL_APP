package com.recall.app.core.ai

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File

object ModelDownloader {

    // Note: Replace with actual hosted URLs for the models
    private const val LLM_MODEL_URL = "https://huggingface.co/google/gemma-2b-it-cpu/resolve/main/gemma-2b-it-cpu.task"
    private const val EMBEDDING_MODEL_URL = "https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2/resolve/main/onnx/model.onnx"
    
    const val LLM_FILE_NAME = "llm_model.task"
    const val EMBEDDING_FILE_NAME = "embedding_model.onnx"

    fun downloadModelsIfMissing(context: Context) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        
        val llmFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), LLM_FILE_NAME)
        if (!llmFile.exists()) {
            val request = DownloadManager.Request(Uri.parse(LLM_MODEL_URL))
                .setTitle("Downloading Recall AI Model")
                .setDescription("Downloading the on-device language model (1.5GB)")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, LLM_FILE_NAME)
                .setAllowedOverMetered(false) // Only over WiFi for 1.5GB!
                .setAllowedOverRoaming(false)
            
            downloadManager.enqueue(request)
        }

        val embedFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), EMBEDDING_FILE_NAME)
        if (!embedFile.exists()) {
            val request = DownloadManager.Request(Uri.parse(EMBEDDING_MODEL_URL))
                .setTitle("Downloading Semantic Search Engine")
                .setDescription("Downloading embedding model")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, EMBEDDING_FILE_NAME)
            
            downloadManager.enqueue(request)
        }
    }
    
    fun getLlmModelFile(context: Context): File {
        return File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), LLM_FILE_NAME)
    }
    
    fun getEmbeddingModelFile(context: Context): File {
        return File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), EMBEDDING_FILE_NAME)
    }
}
