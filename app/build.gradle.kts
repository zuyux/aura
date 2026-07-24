import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

val localEnvironment = Properties().apply {
    val environmentFile = rootProject.file(".env.local")
    if (environmentFile.exists()) {
        environmentFile.inputStream().use { load(it) }
    }
}

fun environmentValue(name: String): String =
    localEnvironment.getProperty(name)
        ?: providers.environmentVariable(name).orNull
        ?: ""

fun normalizeBaseUrl(value: String): String {
    val trimmed = value.trim()
    return if (trimmed.endsWith("/")) trimmed else "$trimmed/"
}

fun buildConfigString(value: String): String =
    "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

val auraDebugApiBaseUrl = providers.gradleProperty("AURA_DEBUG_API_BASE_URL")
    .orElse(providers.gradleProperty("AURA_API_BASE_URL"))
    .orElse("http://10.0.2.2:8080/")
    .map(::normalizeBaseUrl)

val auraReleaseApiBaseUrl = providers.gradleProperty("AURA_PRODUCTION_API_BASE_URL")
    .orElse(providers.gradleProperty("AURA_API_BASE_URL"))
    .orElse("https://api.aura.community/")
    .map(::normalizeBaseUrl)

val googleMapsApiKey = providers.gradleProperty("AURA_GOOGLE_MAPS_API_KEY")
    .orElse(providers.environmentVariable("AURA_GOOGLE_MAPS_API_KEY"))
    .orElse("")

val supabaseUrl = normalizeBaseUrl(environmentValue("SUPABASE_URL").ifBlank {
    "https://example.supabase.co/"
})
val supabasePublishableKey = environmentValue("SUPABASE_PUBLISHABLE_KEY")

fun validateReleaseDatabaseConfiguration() {
    require(supabaseUrl.startsWith("https://") && !supabaseUrl.contains("example.supabase.co")) {
        "Release builds require SUPABASE_URL to be a real HTTPS Supabase project URL."
    }
    require(supabasePublishableKey.isNotBlank()) {
        "Release builds require SUPABASE_PUBLISHABLE_KEY in .env.local or the environment."
    }
    require(!supabasePublishableKey.startsWith("sb_secret_")) {
        "SUPABASE_PUBLISHABLE_KEY must be a publishable/anon key, never a secret key."
    }
}

if (gradle.startParameter.taskNames.any { task -> task.contains("release", ignoreCase = true) }) {
    validateReleaseDatabaseConfiguration()
}

android {
    namespace = "io.aura.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "io.aura.android"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = googleMapsApiKey.get()
        buildConfigField("String", "AURA_SUPABASE_URL", buildConfigString(supabaseUrl))
        buildConfigField("String", "AURA_SUPABASE_PUBLISHABLE_KEY", buildConfigString(supabasePublishableKey))
    }

    buildTypes {
        debug {
            buildConfigField("String", "AURA_API_BASE_URL", buildConfigString(auraDebugApiBaseUrl.get()))
        }
        release {
            buildConfigField("String", "AURA_API_BASE_URL", buildConfigString(auraReleaseApiBaseUrl.get()))
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(enforcedPlatform(libs.compose.bom))
    androidTestImplementation(enforcedPlatform(libs.compose.bom))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)

    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.coil.compose)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.libphonenumber)
    implementation(libs.maps.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgrest)
    implementation(libs.ktor.client.android)

    constraints {
        implementation(libs.androidx.core.ktx) {
            version { strictly(libs.versions.coreKtx.get()) }
        }
        implementation(libs.androidx.lifecycle.runtime.ktx) {
            version { strictly(libs.versions.lifecycle.get()) }
        }
        implementation(libs.androidx.lifecycle.runtime.compose) {
            version { strictly(libs.versions.lifecycle.get()) }
        }
        implementation(libs.androidx.lifecycle.viewmodel.compose) {
            version { strictly(libs.versions.lifecycle.get()) }
        }
        implementation(libs.androidx.browser) {
            version { strictly("1.8.0") }
        }
    }

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)

    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}
