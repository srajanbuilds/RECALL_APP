package com.recall.app.di;

import android.content.Context;
import com.recall.app.core.ai.EmbeddingEngine;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class AiEngineModule_ProvideEmbeddingEngineFactory implements Factory<EmbeddingEngine> {
  private final Provider<Context> contextProvider;

  public AiEngineModule_ProvideEmbeddingEngineFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public EmbeddingEngine get() {
    return provideEmbeddingEngine(contextProvider.get());
  }

  public static AiEngineModule_ProvideEmbeddingEngineFactory create(
      Provider<Context> contextProvider) {
    return new AiEngineModule_ProvideEmbeddingEngineFactory(contextProvider);
  }

  public static EmbeddingEngine provideEmbeddingEngine(Context context) {
    return Preconditions.checkNotNullFromProvides(AiEngineModule.INSTANCE.provideEmbeddingEngine(context));
  }
}
