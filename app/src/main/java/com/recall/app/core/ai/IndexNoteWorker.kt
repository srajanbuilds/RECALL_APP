package com.recall.app.core.ai

import android.content.Context
import android.util.Log
import androidx.work.*
import com.recall.app.core.data.local.AppDatabase
import com.recall.app.core.data.model.VectorEntry
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker that chunks a note and stores embeddings in the DB.
 * Enforces: private notes (isPrivate=true) are NEVER indexed.
 */
class IndexNoteWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_NOTE_ID = "note_id"

        /** Enqueue indexing for a single note. Max 1 concurrent, exponential backoff. */
        fun enqueue(context: Context, noteId: String) {
            val request = OneTimeWorkRequestBuilder<IndexNoteWorker>()
                .setInputData(workDataOf(KEY_NOTE_ID to noteId))
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "index_note_$noteId",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val noteId = inputData.getString(KEY_NOTE_ID)
            ?: return@withContext Result.failure()

        try {
            val db = AppDatabase.getInstance(applicationContext)
            val note = db.noteDao().getNoteById(noteId)
                ?: return@withContext Result.failure()

            // ── ENFORCEMENT: private notes never enter AI pipeline ─────────
            if (note.isPrivate) {
                Log.d("IndexNoteWorker", "Note $noteId is private — skipping indexing")
                return@withContext Result.success()
            }

            val embeddingEngine = EmbeddingEngine(applicationContext)
            val chunks = embeddingEngine.chunkText(note.body)

            if (chunks.isEmpty()) {
                db.noteDao().markNoteIndexed(noteId, indexed = false)
                return@withContext Result.success()
            }

            val entries = chunks.mapIndexed { idx, chunk ->
                val floats = embeddingEngine.generateEmbedding(chunk)
                val buf = ByteBuffer.allocate(floats.size * 4).order(ByteOrder.LITTLE_ENDIAN)
                floats.forEach { buf.putFloat(it) }
                VectorEntry(
                    id = UUID.randomUUID().toString(),
                    noteId = noteId,
                    chunkIndex = idx,
                    chunkText = chunk,
                    embedding = buf.array()
                )
            }

            db.noteDao().deleteVectorEntriesForNote(noteId)
            db.noteDao().insertVectorEntries(entries)
            db.noteDao().markNoteIndexed(noteId, indexed = true)

            Log.d("IndexNoteWorker", "Indexed ${entries.size} chunks for note $noteId")
            Result.success()
        } catch (e: Exception) {
            Log.e("IndexNoteWorker", "Indexing failed for $noteId", e)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
