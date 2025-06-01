// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("com.google.devtools.ksp") version "1.9.0-1.0.13" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.firebase.appdistribution") version "4.2.0" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.google.gms:google-services:4.4.2")
    }
}

tasks.register("cleanGradle") {
    doLast {
        exec {
            commandLine("./gradlew", "--stop")
        }
        delete(fileTree(".gradle"))
        delete(fileTree("build"))
        project.allprojects {
            delete(fileTree("build"))
        }
    }
}

tasks.named("build") {
    finalizedBy("cleanGradle")
}