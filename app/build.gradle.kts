plugins {
    id("com.android.application")
    id("dagger.hilt.android.plugin")
    id("kotlin-android")
    id("kotlin-kapt")
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

    implementation("androidx.activity:activity-compose:1.4.0")
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")
    implementation("com.google.android.material:material:1.4.0")
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")
    kapt("com.google.dagger:hilt-compiler:$hiltVersion")
}
