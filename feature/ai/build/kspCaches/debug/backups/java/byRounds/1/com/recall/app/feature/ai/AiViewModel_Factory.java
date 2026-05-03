package com.recall.app.feature.ai;

import android.app.Application;
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
public final class AiViewModel_Factory implements Factory<AiViewModel> {
  private final Provider<Application> applicationProvider;

  private final Provider<NoteDao> daoProvider;

  public AiViewModel_Factory(Provider<Application> applicationProvider,
      Provider<NoteDao> daoProvider) {
    this.applicationProvider = applicationProvider;
    this.daoProvider = daoProvider;
  }

  @Override
  public AiViewModel get() {
    return newInstance(applicationProvider.get(), daoProvider.get());
  }

  public static AiViewModel_Factory create(Provider<Application> applicationProvider,
      Provider<NoteDao> daoProvider) {
    return new AiViewModel_Factory(applicationProvider, daoProvider);
  }

  public static AiViewModel newInstance(Application application, NoteDao dao) {
    return new AiViewModel(application, dao);
  }
}
