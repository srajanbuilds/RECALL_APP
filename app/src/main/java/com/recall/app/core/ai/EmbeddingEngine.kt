package com.recall.app.core.ai

import android.content.Context
import android.util.Log
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import kotlin.math.sqrt

/**
 * Wraps the all-MiniLM-L6-v2 ONNX model for on-device sentence embeddings.
 * Model file expected at: assets/all-MiniLM-L6-v2.onnx (~22 MB)
 */
class EmbeddingEngine(private val context: Context) {

    private val ortEnv = OrtEnvironment.getEnvironment()
    private var session: OrtSession? = null

    val isReady: Boolean get() = session != null

    init { initialize() }

    private fun initialize() {
        try {
            val bytes = context.assets.open("all-MiniLM-L6-v2.onnx").readBytes()
            val opts = OrtSession.SessionOptions().apply {
                setIntraOpNumThreads(2)
                setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
            }
            session = ortEnv.createSession(bytes, opts)
            Log.i("EmbeddingEngine", "ONNX model loaded successfully")
        } catch (e: Exception) {
            Log.w("EmbeddingEngine", "Model not found in assets — embeddings will be zero vectors. Add all-MiniLM-L6-v2.onnx to app/src/main/assets/")
        }
    }

    /** Returns a 384-dimensional embedding for the given text. */
    fun generateEmbedding(text: String): FloatArray {
        val s = session ?: return FloatArray(384)
        return try {
            val tokens = tokenize(text.take(512))
            val seqLen = tokens.size
            val ids = Array(1) { LongArray(seqLen) { i -> tokens[i].toLong() } }
            val mask = Array(1) { LongArray(seqLen) { 1L } }
            val typeIds = Array(1) { LongArray(seqLen) { 0L } }

            val inputIds = OnnxTensor.createTensor(ortEnv, ids)
            val attMask  = OnnxTensor.createTensor(ortEnv, mask)
            val tokTypes = OnnxTensor.createTensor(ortEnv, typeIds)

            val result = s.run(mapOf(
                "input_ids"      to inputIds,
                "attention_mask" to attMask,
                "token_type_ids" to tokTypes
            ))

            // last_hidden_state: [1, seqLen, 384] → mean pool
            @Suppress("UNCHECKED_CAST")
            val hidden = result[0].value as Array<Array<FloatArray>>
            val pooled = FloatArray(384)
            hidden[0].forEach { vec -> vec.forEachIndexed { i, v -> pooled[i] += v } }
            val len = hidden[0].size.toFloat()
            normalize(FloatArray(384) { pooled[it] / len })
        } catch (e: Exception) {
            Log.e("EmbeddingEngine", "Inference error", e)
            FloatArray(384)
        }
    }

    /** Splits text into overlapping chunks of ~200 tokens each. */
    fun chunkText(text: String, chunkSize: Int = 900, overlap: Int = 90): List<String> {
        if (text.isBlank()) return emptyList()
        if (text.length <= chunkSize) return listOf(text)
        val chunks = mutableListOf<String>()
        var start = 0
        while (start < text.length) {
            val end = minOf(start + chunkSize, text.length)
            val chunk = text.substring(start, end).trim()
            if (chunk.length >= 40) chunks.add(chunk)   // min 10 "tokens" ≈ 40 chars
            start += chunkSize - overlap
        }
        return chunks
    }

    private fun normalize(v: FloatArray): FloatArray {
        val norm = sqrt(v.fold(0f) { acc, x -> acc + x * x })
        return if (norm == 0f) v else FloatArray(v.size) { v[it] / norm }
    }

    /** Minimal character-level tokenizer fallback (proper WordPiece requires vocab file). */
    private fun tokenize(text: String, maxLen: Int = 128): List<Int> {
        val ids = mutableListOf(101) // [CLS]
        text.lowercase().take(maxLen - 2).forEach { c ->
            ids.add(c.code.coerceIn(100, 30000))
        }
        ids.add(102) // [SEP]
        return ids
    }

    companion object {
        /**
         * Exact cosine similarity as specified in Section 6.
         * Both vectors must have length 384.
         */
        fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
            var dot = 0f; var normA = 0f; var normB = 0f
            for (i in a.indices) {
                dot  += a[i] * b[i]
                normA += a[i] * a[i]
                normB += b[i] * b[i]
            }
            return dot / (sqrt(normA) * sqrt(normB))
        }
    }
}
