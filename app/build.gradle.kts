import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("app.accrescent.tools.bundletool") version "0.1.2"
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
    id("kotlin-android")
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.10"
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val useKeystoreProperties = keystorePropertiesFile.canRead()
val keystoreProperties = Properties()
if (useKeystoreProperties) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
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
    buildToolsVersion = "33.0.2"

    defaultConfig {
        applicationId = "app.accrescent.client"
        minSdk = 31
        targetSdk = 33
        versionCode = 27
        versionName = "0.12.2"
        resourceConfigurations.addAll(listOf(
            "az",
            "da",
            "de",
            "en",
            "eo",
            "es",
            "fr",
            "in",
            "it",
            "iw",
            "ja",
            "ko",
            "ms",
            "nb-rNO",
            "nl",
            "pl",
            "pt",
            "ro",
            "ru",
            "sv",
            "ta",
            "tr",
            "uk",
            "zh-rCN",
            "zh-rTW",
        ))

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
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
        kotlinCompilerExtensionVersion = "1.4.4"
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

    lint {
        baseline = file("lint-baseline.xml")
        enable += "ComposeM2Api"
        error += "ComposeM2Api"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2023.01.00"))
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    implementation("androidx.hilt:hilt-work:1.0.0")
    implementation("androidx.lifecycle:lifecycle-process:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("androidx.navigation:navigation-compose:2.5.3")
    implementation("androidx.room:room-ktx:2.5.1")
    implementation("androidx.room:room-runtime:2.5.1")
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    implementation("com.google.accompanist:accompanist-navigation-animation:0.28.0")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.28.0")
    implementation("com.google.android.material:material:1.8.0")
    implementation("com.google.dagger:hilt-android:2.45")
    implementation("io.coil-kt:coil-compose:2.2.2")
    implementation("org.bouncycastle:bcprov-jdk18on:1.72")
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    kapt("androidx.hilt:hilt-compiler:1.0.0")
    ksp("androidx.room:room-compiler:2.5.1")
    kapt("com.google.dagger:hilt-android-compiler:2.45")

    lintChecks("com.slack.lint.compose:compose-lint-checks:1.1.1")
}

kapt {
    correctErrorTypes = true
}
