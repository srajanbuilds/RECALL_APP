package com.recall.app.feature.notes;

import android.app.Application;
import com.recall.app.core.ai.EmbeddingEngine;
import com.recall.app.core.data.local.NoteDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class NotesViewModel_Factory implements Factory<NotesViewModel> {
  private final Provider<Application> applicationProvider;

  private final Provider<NoteDao> daoProvider;

  private final Provider<EmbeddingEngine> embeddingEngineProvider;

  public NotesViewModel_Factory(Provider<Application> applicationProvider,
      Provider<NoteDao> daoProvider, Provider<EmbeddingEngine> embeddingEngineProvider) {
    this.applicationProvider = applicationProvider;
    this.daoProvider = daoProvider;
    this.embeddingEngineProvider = embeddingEngineProvider;
  }

  @Override
  public NotesViewModel get() {
    return newInstance(applicationProvider.get(), daoProvider.get(), embeddingEngineProvider.get());
  }

  public static NotesViewModel_Factory create(Provider<Application> applicationProvider,
      Provider<NoteDao> daoProvider, Provider<EmbeddingEngine> embeddingEngineProvider) {
    return new NotesViewModel_Factory(applicationProvider, daoProvider, embeddingEngineProvider);
  }

  public static NotesViewModel newInstance(Application application, NoteDao dao,
      EmbeddingEngine embeddingEngine) {
    return new NotesViewModel(application, dao, embeddingEngine);
  }
}
