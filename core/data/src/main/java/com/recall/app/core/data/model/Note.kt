package com.recall.app.core.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Represents a single user note within the application.
 *
 * This entity holds the core text content along with metadata such as
 * tags, folder association, and privacy settings. Private notes are strictly
 * excluded from semantic AI indexing and local backups.
 *
 * @property isPrivate If true, this note is excluded from vector embeddings, FTS search for AI context, and exports.
 * @property isAiIndexed Flags whether the background worker has successfully generated vector embeddings for this note.
 */
@Entity(
    tableName = "notes",
    indices = [
        Index("updated_at"),
        Index("folder_id"),
        Index("is_archived")
    ]
)
data class Note(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val body: String,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "folder_id") val folderId: String? = null,
    val tags: String = "[]", // JSON string array for v1
    @ColumnInfo(name = "color_hex") val colorHex: String? = null,
    @ColumnInfo(name = "is_pinned") val isPinned: Boolean = false,
    @ColumnInfo(name = "is_archived") val isArchived: Boolean = false,
    @ColumnInfo(name = "is_private") val isPrivate: Boolean = false,
    @ColumnInfo(name = "is_ai_indexed") val isAiIndexed: Boolean = false
)
