package com.recall.app.di;

import com.recall.app.core.data.local.AppDatabase;
import com.recall.app.core.data.local.NoteDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class DataModule_ProvideNoteDaoFactory implements Factory<NoteDao> {
  private final Provider<AppDatabase> dbProvider;

  public DataModule_ProvideNoteDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public NoteDao get() {
    return provideNoteDao(dbProvider.get());
  }

  public static DataModule_ProvideNoteDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DataModule_ProvideNoteDaoFactory(dbProvider);
  }

  public static NoteDao provideNoteDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DataModule.INSTANCE.provideNoteDao(db));
  }
}
