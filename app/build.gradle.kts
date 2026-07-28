import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties

plugins {

    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.firebase.perf)
}

val isRelease = project.gradle.startParameter.taskNames.any { it.contains("release", ignoreCase = true) }
val localProperties = loadLocalProperties()
applyExternalConfig(localProperties)

android {

    namespace = "com.simple.launcher.retirement"
    compileSdk = 37

    signingConfigs {

        create("keystore") {

            keyAlias = (project.findProperty("keyAlias") ?: "").toString()
            keyPassword = (project.findProperty("keyPassword") ?: "").toString()
            storeFile = project.findProperty("storeFile")?.let { file(it.toString()) }
            storePassword = (project.findProperty("storePassword") ?: "").toString()
        }
    }

    defaultConfig {

        applicationId = "com.simple.launcher.retirement"
        minSdk = 24
        targetSdk = 37

        val gitVersionCode = if (isRelease) {

            getGitVersionCode()
        } else {

            1
        }

        val gitVersionName = if (isRelease) {

            "1.${SimpleDateFormat("yy.MM").format(Date())}.${getGitVersionCode()}"
        } else {

            "debug"
        }

        versionCode = gitVersionCode
        versionName = gitVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        printVersionInfo(gitVersionCode, gitVersionName)
    }

    buildTypes {

        getByName("release") {

            isMinifyEnabled = true
            isShrinkResources = true

            val keystore = signingConfigs.findByName("keystore")
            if (keystore?.storeFile != null) {

                signingConfig = keystore
            }

            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {

        viewBinding = true
        buildConfig = true
    }

    lint {
        disable.add("UnsafeOptInUsageError")
        disable.add("UnsafeOptInUsageWarning")
    }

    compileOptions {

        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {

    compilerOptions {

        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.metrics)
    implementation(libs.gson)
    implementation(libs.adapter)
    ksp(libs.adapter.processor)
    implementation(libs.deeplink)
    ksp(libs.deeplink.processor)
    implementation(libs.auto.register)
    ksp(libs.auto.register.processor)
    implementation(libs.component.service)
    implementation(libs.node.engine)
    implementation(libs.glide.loader)
    implementation(libs.auto.service)
    ksp(libs.auto.service.ksp)
    implementation(libs.glide)
    implementation(libs.glide.transformations)
    implementation(libs.lottie)
    implementation(libs.androidx.work.runtime.ktx)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.perf)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

fun loadLocalProperties(): Properties {

    val properties = Properties()
    val file = project.rootProject.file("local.properties")
    if (file.exists()) {

        file.inputStream().use { properties.load(it) }
    }
    return properties
}

fun applyExternalConfig(localProperties: Properties) {

    val retirementPath = localProperties.getProperty("module.config") ?: return
    val configFile = file("${retirementPath}/config.gradle.kts")
    if (configFile.exists()) {

        apply(from = configFile)
    }
}

fun runGitCommand(command: List<String>, defaultValue: Any): Any {

    return try {

        val process = ProcessBuilder(command)
            .directory(rootProject.rootDir)
            .redirectErrorStream(true)
            .start()

        process.waitFor()
        processCommandOutput(process, defaultValue)
    } catch (e: Exception) {

        println("⚠️ Git command failed: ${e.message}")
        defaultValue
    }
}

fun processCommandOutput(process: Process, defaultValue: Any): Any {

    if (process.exitValue() != 0) return defaultValue

    val output = process.inputStream.bufferedReader().readText().trim()
    if (output.isEmpty()) return defaultValue

    return if (defaultValue is Int) output.toInt() else output
}

fun getGitVersionCode(): Int {

    return runGitCommand(
        listOf("git", "rev-list", "--count", "HEAD"),
        1
    ) as Int
}

fun getGitHash(): String {

    return runGitCommand(
        listOf("git", "rev-parse", "--short", "HEAD"),
        "dev"
    ) as String
}

fun printVersionInfo(versionCode: Int, versionName: String) {

    println("-------------------------------------------")
    println("🚀 Building RetirementLauncher")
    println("📌 Version Code: ${versionCode}")
    println("📌 Version Name: ${versionName}")
    println("-------------------------------------------")
}
