import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

apply(plugin = "com.github.ben-manes.versions")

buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        sonatypeSnapshots()
    }

    apply(from = "$rootDir/gradle/dependencies.gradle")

    dependencies {
        classpath(buildscriptDependency("android"))
        classpath(buildscriptDependency("kotlin"))
        classpath(buildscriptDependency("navigation"))
        classpath(buildscriptDependency("androidJUnit5"))
        classpath(buildscriptDependency("versions"))
        classpath(buildscriptDependency("sqldelight"))
        classpath(buildscriptDependency("hilt"))
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        sonatypeSnapshots()

        @Suppress("JcenterRepositoryObsolete")
        jcenter {
            mavenContent {
                includeModule("com.jsibbold", "zoomage")
            }
        }
    }

    apply(from = "$rootDir/gradle/dependencies.gradle")
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { it in version.toUpperCase() }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}
