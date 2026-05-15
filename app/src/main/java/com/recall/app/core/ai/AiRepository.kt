package com.recall.app.core.ai

import android.content.Context
import com.recall.app.room.BaseNote
import java.io.File
import java.nio.ByteBuffer

object AiRepository {
    private var embeddingEngine: EmbeddingEngine? = null
    private var llamaEngine: LlamaEngine? = null

    fun getEmbeddingEngine(context: Context): EmbeddingEngine {
        if (embeddingEngine == null) {
            val modelFile = ModelDownloader.getEmbeddingModelFile(context)
            embeddingEngine = EmbeddingEngine(modelFile)
        }
        return embeddingEngine!!
    }

    fun getLlamaEngine(context: Context): LlamaEngine {
        if (llamaEngine == null) {
            val modelFile = ModelDownloader.getLlmModelFile(context)
            llamaEngine = LlamaEngine(modelFile).apply { initialize(context) }
        }
        return llamaEngine!!
    }

    /**
     * Compute cosine similarity between two float arrays.
     */
    fun cosineSimilarity(vectorA: FloatArray, vectorB: FloatArray): Float {
        if (vectorA.size != vectorB.size) return 0f
        var dotProduct = 0f
        var normA = 0f
        var normB = 0f
        for (i in vectorA.indices) {
            dotProduct += vectorA[i] * vectorB[i]
            normA += vectorA[i] * vectorA[i]
            normB += vectorB[i] * vectorB[i]
        }
        return if (normA == 0f || normB == 0f) 0f else (dotProduct / (Math.sqrt(normA.toDouble()) * Math.sqrt(normB.toDouble()))).toFloat()
    }

    /**
     * Performs a semantic vector search across all provided notes based on a text query.
     */
    fun searchNotes(context: Context, query: String, notes: List<BaseNote>, topK: Int = 3): List<BaseNote> {
        val engine = getEmbeddingEngine(context)
        val queryEmbedding = engine.getEmbedding(query)
        
        return notes
            .filter { it.embedding != null }
            .map { note ->
                val floatBuffer = ByteBuffer.wrap(note.embedding!!).asFloatBuffer()
                val noteEmbedding = FloatArray(floatBuffer.remaining())
                floatBuffer.get(noteEmbedding)
                
                val score = cosineSimilarity(queryEmbedding, noteEmbedding)
                Pair(note, score)
            }
            .sortedByDescending { it.second }
            .take(topK)
            .map { it.first }
    }
}
