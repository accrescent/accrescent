import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("dagger.hilt.android.plugin")
    id("kotlin-android")
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.6.10"
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val useKeystoreProperties = keystorePropertiesFile.canRead()
val keystoreProperties = Properties()
if (useKeystoreProperties) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    if (useKeystoreProperties) {
        signingConfigs {
            create("release") {
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
                storeFile = file(keystoreProperties["storeFile"]!!)
                storePassword = keystoreProperties["storePassword"] as String
                enableV2Signing = false
                enableV3Signing = true
                enableV4Signing = true
            }
        }
    }

    compileSdk = 32

    defaultConfig {
        applicationId = "app.accrescent.client"
        minSdk = 31
        targetSdk = 31
        versionCode = 1
        versionName = "0.1.0"

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
            if (useKeystoreProperties) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_11)
        targetCompatibility(JavaVersion.VERSION_11)
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.1.1"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.activity:activity-compose:1.4.0")
    implementation("androidx.compose.material:material:1.1.1")
    implementation("androidx.compose.ui:ui:1.1.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.1.1")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.hilt:hilt-work:1.0.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.4.0")
    implementation("androidx.room:room-ktx:2.4.2")
    implementation("androidx.room:room-runtime:2.4.2")
    implementation("androidx.work:work-runtime-ktx:2.7.1")
    implementation("com.google.android.material:material:1.4.0")
    implementation("com.google.dagger:hilt-android:2.41")
    implementation("org.bouncycastle:bcprov-jdk15to18:1.70")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    debugImplementation("androidx.compose.ui:ui-tooling:1.1.1")
    kapt("androidx.hilt:hilt-compiler:1.0.0")
    kapt("androidx.room:room-compiler:2.4.2")
    kapt("com.google.dagger:hilt-android-compiler:2.41")
}

kapt {
    correctErrorTypes = true
}
