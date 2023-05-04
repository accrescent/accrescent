plugins {
    id("com.android.application") version "8.0.1" apply false
    id("org.jetbrains.kotlin.android") version "1.8.21" apply false
    id("com.google.devtools.ksp") version "1.8.21-1.0.11" apply false
    id("com.google.dagger.hilt.android") version "2.45" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
