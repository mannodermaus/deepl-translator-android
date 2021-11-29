buildscript {
    repositories {
        google()
        mavenCentral()
    }

    apply(from = "../gradle/dependencies.gradle")
}

repositories {
    google()
    mavenCentral()
}

plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(gradleApi())
}
