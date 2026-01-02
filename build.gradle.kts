// Top-level build file: qui si definiscono i plugin per tutto il progetto
plugins {
    // Plugin per l'applicazione Android
    alias(libs.plugins.android.application) apply false

    // Plugin per il supporto a Kotlin in Android
    alias(libs.plugins.kotlin.android) apply false

    // Plugin per Jetpack Compose (necessario per la UI moderna)
    alias(libs.plugins.kotlin.compose) apply false
}

// Solitamente i file top-level moderni terminano qui.
// Le dipendenze (come Retrofit o la Splash Screen) vanno invece nel file:
// app/build.gradle.kts
