plugins {
    id("com.android.application")
    id("dagger.hilt.android.plugin")
    id("kotlin-android")
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.5.31"
}

val composeVersion: String by rootProject.extra

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "net.lberrymage.accrescent"
        minSdk = 31
        targetSdk = 31
        versionCode = 1
        versionName = "0.0.0"

        vectorDrawables {
            useSupportLibrary = true
        }

        kapt {
            arguments {
                arg("room.schemaLocation", "$projectDir/schemas")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = composeVersion
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val hiltVersion: String by rootProject.extra
    val lifecycleVersion: String by rootProject.extra
    val roomVersion: String by rootProject.extra

    implementation("androidx.activity:activity-compose:1.4.0")
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    implementation("androidx.hilt:hilt-work:1.0.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.work:work-runtime-ktx:2.7.1")
    implementation("com.google.android.material:material:1.4.0")
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    implementation("org.bouncycastle:bcprov-jdk15to18:1.70")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")
    kapt("androidx.hilt:hilt-compiler:1.0.0")
    kapt("androidx.room:room-compiler:$roomVersion")
    kapt("com.google.dagger:hilt-android-compiler:$hiltVersion")
}

kapt {
    correctErrorTypes = true
}
