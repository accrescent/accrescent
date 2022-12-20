import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("dagger.hilt.android.plugin")
    id("kotlin-android")
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.7.20"
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

    namespace = "app.accrescent.client"
    compileSdk = 33
    buildToolsVersion = "33.0.1"

    defaultConfig {
        applicationId = "app.accrescent.client"
        minSdk = 31
        targetSdk = 33
        versionCode = 18
        versionName = "0.8.0"
        resourceConfigurations.addAll(listOf(
            "da",
            "de",
            "en",
            "eo",
            "es",
            "fr",
            "in",
            "it",
            "ko",
            "nb-rNO",
            "nl",
            "pl",
            "ro",
            "ru",
            "sv",
            "tr",
            "uk",
            "zh-rCN",
            "zh-rTW",
        ))

        kapt {
            arguments {
                arg("room.schemaLocation", "$projectDir/schemas")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".dev"
        }

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
        freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.2"
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
    packagingOptions {
        resources.excludes.addAll(listOf(
            "DebugProbesKt.bin",
            "META-INF/**.version",
            "kotlin-tooling-metadata.json",
            "kotlin/**.kotlin_builtins",
            "org/bouncycastle/pqc/**.properties",
            "org/bouncycastle/x509/**.properties",
        ))
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2022.12.00"))
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    implementation("androidx.hilt:hilt-work:1.0.0")
    implementation("androidx.lifecycle:lifecycle-process:2.5.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1")
    implementation("androidx.navigation:navigation-compose:2.5.3")
    implementation("androidx.room:room-ktx:2.4.3")
    implementation("androidx.room:room-runtime:2.4.3")
    implementation("androidx.work:work-runtime-ktx:2.7.1")
    implementation("com.google.accompanist:accompanist-navigation-animation:0.28.0")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.28.0")
    implementation("com.google.android.material:material:1.7.0")
    implementation("com.google.dagger:hilt-android:2.44.2")
    implementation("org.bouncycastle:bcprov-jdk18on:1.72")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    kapt("androidx.hilt:hilt-compiler:1.0.0")
    kapt("androidx.room:room-compiler:2.4.3")
    kapt("com.google.dagger:hilt-android-compiler:2.44.2")
}

kapt {
    correctErrorTypes = true
}
