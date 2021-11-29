import java.util.Properties

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("dagger.hilt.android.plugin")
}

val javaVersion = JavaVersion.VERSION_11
val localProperties = Properties().also { it.load(rootProject.file("local.properties").reader()) }

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "de.mannodermaus.blabberl"
        minSdk = 24
        targetSdk = 31
        versionCode = 1
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField(
            "String",
            "DEEPL_AUTH_KEY",
            localProperties.getProperty("DEEPL_AUTH_KEY")
        )
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        isCoreLibraryDesugaringEnabled = true
    }

    @Suppress("SuspiciousCollectionReassignment")
    kotlinOptions {
        jvmTarget = javaVersion.toString()

        freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
        freeCompilerArgs += "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        freeCompilerArgs += "-Xopt-in=kotlin.time.ExperimentalTime"
        freeCompilerArgs += "-Xopt-in=kotlin.contracts.ExperimentalContracts"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = version("compose")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    packagingOptions {
        resources.excludes.add("META-INF/AL2.0")
        resources.excludes.add("META-INF/LGPL2.1")
    }
}

kapt {
    correctErrorTypes = true
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion(version("kotlin"))
        }
    }
}

dependencies {
    coreLibraryDesugaring(dependency("desugar"))
    implementation(kotlin("stdlib"))

    implementation(dependency("bimap"))

    // Coroutines
    implementation(dependency("coroutinesCore"))
    implementation(dependency("coroutinesAndroid"))

    // AndroidX & Material Design
    implementation(dependency("material"))
    implementation(dependency("androidXFragment"))
    implementation(dependency("androidXNavigationFragment"))
    implementation(dependency("androidXNavigationUi"))
    implementation(dependency("androidXCore"))

    // Jetpack Compose
    implementation(dependency("androidXActivityCompose"))
    implementation(dependency("androidXViewModelCompose"))
    implementation(dependency("androidXPagingCompose"))
    implementation(dependency("composeUi"))
    implementation(dependency("composeTooling"))
    implementation(dependency("composeFoundation"))
    implementation(dependency("composeMaterial"))
    implementation(dependency("accompanistInsets"))
    implementation(dependency("accompanistPermissions"))

    // Dependency Injection
    implementation(dependency("hiltNavigationCompose"))
    implementation(dependency("hiltAndroid"))
    kapt(dependency("hiltAndroidCompiler"))

    // Serialization & Persistence
    implementation(dependency("moshi"))
    kapt(dependency("moshiCodegen"))

    // Network
    implementation(dependency("okHttp"))
    implementation(dependency("retrofit"))
    implementation(dependency("retrofitMoshiConverter"))

    // Logging
    implementation(dependency("timber"))

    // Test
    testImplementation(kotlin("stdlib"))
    testImplementation(testDependency("junitJupiterApi"))
    testImplementation(testDependency("junitJupiterParams"))
    testRuntimeOnly(testDependency("junitJupiterEngine"))
    testImplementation(testDependency("sqldelightSqliteDriver"))
}
