package com.recall.app.feature.reminders;

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
public final class RemindersViewModel_Factory implements Factory<RemindersViewModel> {
  private final Provider<NoteDao> daoProvider;

  public RemindersViewModel_Factory(Provider<NoteDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public RemindersViewModel get() {
    return newInstance(daoProvider.get());
  }

  public static RemindersViewModel_Factory create(Provider<NoteDao> daoProvider) {
    return new RemindersViewModel_Factory(daoProvider);
  }

  public static RemindersViewModel newInstance(NoteDao dao) {
    return new RemindersViewModel(dao);
  }
}
