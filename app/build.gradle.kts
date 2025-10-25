// SPDX-FileCopyrightText: © 2021 The Accrescent Contributors
//
// SPDX-License-Identifier: Apache-2.0

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
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
    jvmToolchain(21)

    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
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
    compileSdk = 36
    buildToolsVersion = "36.0.0"

    androidResources {
        localeFilters.addAll(listOf(
            "ar",
            "az",
            "bg",
            "bn",
            "ca",
            "cs",
            "da",
            "de",
            "en",
            "eo",
            "es",
            "et",
            "fr",
            "gl",
            "hu",
            "in",
            "it",
            "iw",
            "ja",
            "ka",
            "ko",
            "lt",
            "lv",
            "ml",
            "ms",
            "nb-rNO",
            "nl",
            "nn",
            "or",
            "pl",
            "pt",
            "pt-rBR",
            "ro",
            "ru",
            "sl",
            "sr",
            "sv",
            "ta",
            "tr",
            "uk",
            "vi",
            "zh-rCN",
            "zh-rTW",
        ))
    }

    defaultConfig {
        applicationId = "app.accrescent.client"
        minSdk = 29
        targetSdk = 36
        versionCode = 53
        versionName = "0.27.0"
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
        sourceCompatibility(JavaVersion.VERSION_21)
        targetCompatibility(JavaVersion.VERSION_21)
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
            "**/MANIFEST.MF",
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

ksp {
    arg("room.generateKotlin", true.toString())
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(libs.activity.compose)
    implementation(libs.appstore.api)
    implementation(libs.bouncycastle)
    implementation(libs.coil)
    implementation(libs.coil.network.okhttp)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material.icons)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.dagger)
    implementation(libs.datastore)
    implementation(libs.grpc.android)
    implementation(libs.grpc.okhttp)
    implementation(libs.hilt.lifecycle.viewmodel.compose)
    implementation(libs.hilt.work)
    implementation(libs.immutable)
    implementation(libs.lifecycle.process)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.material)
    implementation(libs.navcompose)
    implementation(libs.okhttp)
    implementation(libs.okhttp.coroutines)
    implementation(libs.paging.compose)
    implementation(libs.room.runtime)
    implementation(libs.serialization)
    implementation(libs.work.runtime)
    ksp(libs.dagger.compiler)
    ksp(libs.hilt.compiler)
    ksp(libs.room.compiler)

    lintChecks(libs.compose.lint)
}
