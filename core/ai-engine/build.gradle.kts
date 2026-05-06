// :core:ai-engine — ONNX embeddings, MediaPipe LLM, RAG pipeline, IndexNoteWorker
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}
android {
    namespace = "com.recall.app.core.aiengine"
    compileSdk = 35
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:domain"))
    implementation(project(":core:common"))

    // Room runtime needed to access AppDatabase.getInstance()
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // ONNX Runtime — bundled MiniLM model
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.17.0")

    // MediaPipe LLM Inference (removed to prevent libc++_shared.so collision with ONNX)
    // implementation("com.google.mediapipe:tasks-genai:0.10.14")

    // WorkManager for IndexNoteWorker
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.hilt:hilt-work:1.2.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51")
    ksp("com.google.dagger:hilt-android-compiler:2.51")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}
