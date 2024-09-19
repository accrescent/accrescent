import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.accrescent.bundletool)
    alias(libs.plugins.dagger)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val useKeystoreProperties = keystorePropertiesFile.canRead()
val keystoreProperties = Properties()
if (useKeystoreProperties) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

kotlin {
    jvmToolchain(11)
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
    compileSdk = 34
    buildToolsVersion = "34.0.0"

    defaultConfig {
        applicationId = "app.accrescent.client"
        minSdk = 29
        targetSdk = 35
        versionCode = 47
        versionName = "0.23.0"
        resourceConfigurations.addAll(listOf(
            "ar",
            "az",
            "cs",
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
            "lt",
            "ml",
            "ms",
            "nb-rNO",
            "nl",
            "nn",
            "or",
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
            arg("room.generateKotlin", true.toString())
        }
    }

    bundle {
        language {
            enableSplit = false
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
        buildConfig = true
        compose = true
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
    packaging {
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
        enable += "ComposeM2Api"
        error += "ComposeM2Api"
    }
}

if (useKeystoreProperties) {
    bundletool {
        signingConfig {
            storeFile = file(keystoreProperties["storeFile"]!!)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(libs.activity.compose)
    implementation(libs.bouncycastle)
    implementation(libs.coil)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.dagger)
    implementation(libs.datastore)
    implementation(libs.hilt.navcompose)
    implementation(libs.hilt.work)
    implementation(libs.immutable)
    implementation(libs.lifecycle.process)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.material)
    implementation(libs.navcompose)
    implementation(libs.room.ktx)
    implementation(libs.room.runtime)
    implementation(libs.serialization)
    implementation(libs.work.runtime)
    ksp(libs.dagger.compiler)
    ksp(libs.hilt.compiler)
    ksp(libs.room.compiler)

    lintChecks(libs.compose.lint)
}
