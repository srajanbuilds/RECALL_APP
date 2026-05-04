package com.recall.app.core.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Represents a scheduled alarm or reminder associated with a specific note.
 *
 * Reminders are scheduled via Android's AlarmManager. If the parent note is deleted,
 * its associated reminders are cascaded and deleted automatically by Room.
 *
 * @property triggerAt The Unix timestamp (in milliseconds) when the reminder should fire.
 * @property isCompleted Indicates if the user has dismissed or completed this reminder.
 */
@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = Note::class,
            parentColumns = ["id"],
            childColumns = ["note_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("trigger_at"), Index("note_id")]
)
data class Reminder(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "note_id") val noteId: String,
    @ColumnInfo(name = "trigger_at") val triggerAt: Long,
    val label: String,
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean = false
)
