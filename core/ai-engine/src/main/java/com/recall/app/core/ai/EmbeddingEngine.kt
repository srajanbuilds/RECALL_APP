package com.recall.app.core.ai

import android.content.Context
import kotlin.math.sqrt

/**
 * A mocked local inference engine for the Safe Core Rewrite.
 * Returns dummy vectors.
 */
class EmbeddingEngine(private val context: Context) {

    val isReady: Boolean get() = true

    /**
     * Generates a zeroed 384-dimensional vector embedding.
     */
    fun generateEmbedding(text: String): FloatArray {
        return FloatArray(384)
    }

    /**
     * Splits a large body of text into overlapping contiguous chunks.
     */
    fun chunkText(text: String, chunkSize: Int = 900, overlap: Int = 90): List<String> {
        if (text.isBlank()) return emptyList()
        if (text.length <= chunkSize) return listOf(text)
        val chunks = mutableListOf<String>()
        var start = 0
        while (start < text.length) {
            val end = minOf(start + chunkSize, text.length)
            val chunk = text.substring(start, end).trim()
            if (chunk.length >= 40) chunks.add(chunk)
            start += chunkSize - overlap
        }
        return chunks
    }

    companion object {
        fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
            return 0f
        }
    }
}
