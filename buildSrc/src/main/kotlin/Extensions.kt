import groovy.lang.Closure
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.maven

/* RepositoryHandler */

fun RepositoryHandler.jitpack() = maven(url = "https://jitpack.io")

fun RepositoryHandler.sonatypeSnapshots() =
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots") {
        mavenContent {
            snapshotsOnly()
        }
    }

@Suppress("UNCHECKED_CAST")
fun Project.dependency(scope: String, id: String): String {
    val allDeps = requireNotNull(this.extra["dependencies"] as Map<String, Map<String, Any>>) {
        "Could not find dependencies list on $this. Make sure to apply 'dependencies.gradle' first"
    }
    val scopeDeps = requireNotNull(allDeps[scope]) {
        "No scope named '$scope' found in dependencies.gradle"
    }
    val dep = requireNotNull(scopeDeps[id]) {
        "No dependency named '$id' found in scope '$scope' within dependencies.gradle"
    }

    return if (dep is Closure<*>) {
        dep.invoke().toString()
    } else {
        dep.toString()
    }
}

fun Project.buildscriptDependency(id: String): String =
    dependency(scope = "buildscript", id = id)

fun Project.dependency(id: String): String =
    dependency(scope = "libraries", id = id)

fun Project.testDependency(id: String): String =
    dependency(scope = "test", id = id)

@Suppress("UNCHECKED_CAST")
fun Project.version(id: String): String {
    val allVersions = requireNotNull(this.extra["versions"] as Map<String, Any>) {
        "Could not find versions list on $this. Make sure to apply 'dependencies.gradle' first"
    }
    val version = requireNotNull(allVersions[id])

    return if (version is Closure<*>) {
        version.invoke().toString()
    } else {
        version.toString()
    }
}
