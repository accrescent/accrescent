buildscript {
    repositories {
        // We declare repositories this way because dependabot doesn't pick up all our dependencies
        // otherwise. Relevant issue: https://github.com/dependabot/dependabot-core/issues/3901.
        maven { url = uri("https://dl.google.com/dl/android/maven2")} // google()
        maven { url = uri("https://repo.maven.apache.org/maven2")} // mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.3.1")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.44.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.22")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
