package com.recall.app.core.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.FtsTableInfo;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile NoteDao _noteDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `notes` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `body` TEXT NOT NULL, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, `folder_id` TEXT, `tags` TEXT NOT NULL, `color_hex` TEXT, `is_pinned` INTEGER NOT NULL, `is_archived` INTEGER NOT NULL, `is_private` INTEGER NOT NULL, `is_ai_indexed` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_updated_at` ON `notes` (`updated_at`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_folder_id` ON `notes` (`folder_id`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_is_archived` ON `notes` (`is_archived`)");
        db.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS `notes_fts` USING FTS4(`title` TEXT NOT NULL, `body` TEXT NOT NULL, `note_id` TEXT NOT NULL, tokenize=unicode61)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `folders` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `created_at` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `reminders` (`id` TEXT NOT NULL, `note_id` TEXT NOT NULL, `trigger_at` INTEGER NOT NULL, `label` TEXT NOT NULL, `is_completed` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`note_id`) REFERENCES `notes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_reminders_trigger_at` ON `reminders` (`trigger_at`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_reminders_note_id` ON `reminders` (`note_id`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `vector_entries` (`id` TEXT NOT NULL, `note_id` TEXT NOT NULL, `chunk_index` INTEGER NOT NULL, `chunk_text` TEXT NOT NULL, `embedding` BLOB NOT NULL, `updated_at` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`note_id`) REFERENCES `notes`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_vector_entries_note_id` ON `vector_entries` (`note_id`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '9a8bbf10a55643e8d9d7f1ca9379d651')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `notes`");
        db.execSQL("DROP TABLE IF EXISTS `notes_fts`");
        db.execSQL("DROP TABLE IF EXISTS `folders`");
        db.execSQL("DROP TABLE IF EXISTS `reminders`");
        db.execSQL("DROP TABLE IF EXISTS `vector_entries`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsNotes = new HashMap<String, TableInfo.Column>(12);
        _columnsNotes.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("body", new TableInfo.Column("body", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("created_at", new TableInfo.Column("created_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("updated_at", new TableInfo.Column("updated_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("folder_id", new TableInfo.Column("folder_id", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("tags", new TableInfo.Column("tags", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("color_hex", new TableInfo.Column("color_hex", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("is_pinned", new TableInfo.Column("is_pinned", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("is_archived", new TableInfo.Column("is_archived", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("is_private", new TableInfo.Column("is_private", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotes.put("is_ai_indexed", new TableInfo.Column("is_ai_indexed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysNotes = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesNotes = new HashSet<TableInfo.Index>(3);
        _indicesNotes.add(new TableInfo.Index("index_notes_updated_at", false, Arrays.asList("updated_at"), Arrays.asList("ASC")));
        _indicesNotes.add(new TableInfo.Index("index_notes_folder_id", false, Arrays.asList("folder_id"), Arrays.asList("ASC")));
        _indicesNotes.add(new TableInfo.Index("index_notes_is_archived", false, Arrays.asList("is_archived"), Arrays.asList("ASC")));
        final TableInfo _infoNotes = new TableInfo("notes", _columnsNotes, _foreignKeysNotes, _indicesNotes);
        final TableInfo _existingNotes = TableInfo.read(db, "notes");
        if (!_infoNotes.equals(_existingNotes)) {
          return new RoomOpenHelper.ValidationResult(false, "notes(com.recall.app.core.data.model.Note).\n"
                  + " Expected:\n" + _infoNotes + "\n"
                  + " Found:\n" + _existingNotes);
        }
        final HashSet<String> _columnsNotesFts = new HashSet<String>(3);
        _columnsNotesFts.add("title");
        _columnsNotesFts.add("body");
        _columnsNotesFts.add("note_id");
        final FtsTableInfo _infoNotesFts = new FtsTableInfo("notes_fts", _columnsNotesFts, "CREATE VIRTUAL TABLE IF NOT EXISTS `notes_fts` USING FTS4(`title` TEXT NOT NULL, `body` TEXT NOT NULL, `note_id` TEXT NOT NULL, tokenize=unicode61)");
        final FtsTableInfo _existingNotesFts = FtsTableInfo.read(db, "notes_fts");
        if (!_infoNotesFts.equals(_existingNotesFts)) {
          return new RoomOpenHelper.ValidationResult(false, "notes_fts(com.recall.app.core.data.model.NoteFts).\n"
                  + " Expected:\n" + _infoNotesFts + "\n"
                  + " Found:\n" + _existingNotesFts);
        }
        final HashMap<String, TableInfo.Column> _columnsFolders = new HashMap<String, TableInfo.Column>(3);
        _columnsFolders.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFolders.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFolders.put("created_at", new TableInfo.Column("created_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysFolders = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesFolders = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoFolders = new TableInfo("folders", _columnsFolders, _foreignKeysFolders, _indicesFolders);
        final TableInfo _existingFolders = TableInfo.read(db, "folders");
        if (!_infoFolders.equals(_existingFolders)) {
          return new RoomOpenHelper.ValidationResult(false, "folders(com.recall.app.core.data.model.Folder).\n"
                  + " Expected:\n" + _infoFolders + "\n"
                  + " Found:\n" + _existingFolders);
        }
        final HashMap<String, TableInfo.Column> _columnsReminders = new HashMap<String, TableInfo.Column>(5);
        _columnsReminders.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminders.put("note_id", new TableInfo.Column("note_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminders.put("trigger_at", new TableInfo.Column("trigger_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminders.put("label", new TableInfo.Column("label", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminders.put("is_completed", new TableInfo.Column("is_completed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysReminders = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysReminders.add(new TableInfo.ForeignKey("notes", "CASCADE", "NO ACTION", Arrays.asList("note_id"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesReminders = new HashSet<TableInfo.Index>(2);
        _indicesReminders.add(new TableInfo.Index("index_reminders_trigger_at", false, Arrays.asList("trigger_at"), Arrays.asList("ASC")));
        _indicesReminders.add(new TableInfo.Index("index_reminders_note_id", false, Arrays.asList("note_id"), Arrays.asList("ASC")));
        final TableInfo _infoReminders = new TableInfo("reminders", _columnsReminders, _foreignKeysReminders, _indicesReminders);
        final TableInfo _existingReminders = TableInfo.read(db, "reminders");
        if (!_infoReminders.equals(_existingReminders)) {
          return new RoomOpenHelper.ValidationResult(false, "reminders(com.recall.app.core.data.model.Reminder).\n"
                  + " Expected:\n" + _infoReminders + "\n"
                  + " Found:\n" + _existingReminders);
        }
        final HashMap<String, TableInfo.Column> _columnsVectorEntries = new HashMap<String, TableInfo.Column>(6);
        _columnsVectorEntries.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVectorEntries.put("note_id", new TableInfo.Column("note_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVectorEntries.put("chunk_index", new TableInfo.Column("chunk_index", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVectorEntries.put("chunk_text", new TableInfo.Column("chunk_text", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVectorEntries.put("embedding", new TableInfo.Column("embedding", "BLOB", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVectorEntries.put("updated_at", new TableInfo.Column("updated_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysVectorEntries = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysVectorEntries.add(new TableInfo.ForeignKey("notes", "CASCADE", "NO ACTION", Arrays.asList("note_id"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesVectorEntries = new HashSet<TableInfo.Index>(1);
        _indicesVectorEntries.add(new TableInfo.Index("index_vector_entries_note_id", false, Arrays.asList("note_id"), Arrays.asList("ASC")));
        final TableInfo _infoVectorEntries = new TableInfo("vector_entries", _columnsVectorEntries, _foreignKeysVectorEntries, _indicesVectorEntries);
        final TableInfo _existingVectorEntries = TableInfo.read(db, "vector_entries");
        if (!_infoVectorEntries.equals(_existingVectorEntries)) {
          return new RoomOpenHelper.ValidationResult(false, "vector_entries(com.recall.app.core.data.model.VectorEntry).\n"
                  + " Expected:\n" + _infoVectorEntries + "\n"
                  + " Found:\n" + _existingVectorEntries);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "9a8bbf10a55643e8d9d7f1ca9379d651", "ec1ab2779660a3d30b23b9f04662557b");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(1);
    _shadowTablesMap.put("notes_fts", "notes_fts_content");
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "notes","notes_fts","folders","reminders","vector_entries");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `notes`");
      _db.execSQL("DELETE FROM `notes_fts`");
      _db.execSQL("DELETE FROM `folders`");
      _db.execSQL("DELETE FROM `reminders`");
      _db.execSQL("DELETE FROM `vector_entries`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(NoteDao.class, NoteDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public NoteDao noteDao() {
    if (_noteDao != null) {
      return _noteDao;
    } else {
      synchronized(this) {
        if(_noteDao == null) {
          _noteDao = new NoteDao_Impl(this);
        }
        return _noteDao;
      }
    }
  }
}
