package com.recall.app.core.ai;

import android.content.Context;
import androidx.work.WorkerParameters;
import dagger.internal.DaggerGenerated;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class IndexNoteWorker_Factory {
  public IndexNoteWorker_Factory() {
  }

  public IndexNoteWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params);
  }

  public static IndexNoteWorker_Factory create() {
    return new IndexNoteWorker_Factory();
  }

  public static IndexNoteWorker newInstance(Context context, WorkerParameters params) {
    return new IndexNoteWorker(context, params);
  }
}
