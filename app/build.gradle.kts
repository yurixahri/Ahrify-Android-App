plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.yurixahri.ahrify"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.yurixahri.ahrify"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.volley)
    implementation(libs.library)
    implementation(libs.glide)
    implementation(libs.exoplayer)

    implementation(libs.media3.exoplayer)
    implementation(libs.media3.session)
    implementation(libs.media)
    implementation (libs.media3.ffmpeg.decoder)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}