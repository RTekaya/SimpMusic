plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.maxrave.simpmusic.wear"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.maxrave.simpmusic"
        minSdk = 30
        targetSdk = 36
        versionCode = libs.versions.version.code.get().toInt()
        versionName = libs.versions.version.name.get()
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    // Wear Compose UI
    implementation(libs.wear.compose.material)
    implementation(libs.wear.compose.foundation)
    implementation(libs.wear.compose.navigation)
    implementation(libs.wear)

    // DataLayer (phone ↔ watch sync)
    implementation(libs.play.services.wearable)
    implementation(libs.coroutines.play.services)

    // Android + Compose base
    implementation(libs.activity.compose)
    implementation(libs.core.ktx)
    implementation(libs.coroutines.android)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.lifecycle.viewmodelCompose)

    // Image loading for artwork
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
}
