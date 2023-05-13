plugins {
    id("com.google.devtools.ksp") version "1.8.21-1.0.11" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.0.1")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.46.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.21")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
