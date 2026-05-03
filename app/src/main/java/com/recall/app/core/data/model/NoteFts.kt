package com.recall.app.core.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

@Fts4(contentEntity = Note::class, tokenizer = "unicode61")
@Entity(tableName = "notes_fts")
data class NoteFts(
    val title: String,
    val body: String,
    @ColumnInfo(name = "rowid") val rowId: Int
)
