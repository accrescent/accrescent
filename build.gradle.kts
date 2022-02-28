buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.1.1")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.40.5")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
