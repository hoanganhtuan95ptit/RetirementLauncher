import java.util.Properties
import java.io.File

pluginManagement {

    repositories {

        google {

            content {

                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {

    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {

    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {

        google()
        mavenCentral()
        mavenLocal()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "RetirementLauncher"
include(":app")

setupCompositeBuild()

fun setupCompositeBuild() {

    val localProperties = Properties()
    val localPropertiesFile = File(settings.rootDir, "local.properties")
    if (!localPropertiesFile.exists()) return

    localPropertiesFile.inputStream().use { 

        localProperties.load(it)
    }

    val rootProjects = findRootProjects(localProperties)
    rootProjects.forEach { (rootDir, modules) ->

        configureIncludeBuild(rootDir, modules)
    }
}

fun findRootProjects(localProperties: Properties): Map<String, List<String>> {

    val rootProjects = mutableMapOf<String, MutableList<String>>()

    localProperties.forEach { key, path ->

        val keyStr = key.toString()
        val isModule = keyStr.startsWith("module.")
        val isNotConfig = keyStr != "module.config"

        if (isModule && isNotConfig) {

            processModuleProperty(keyStr, path.toString(), rootProjects)
        }
    }
    return rootProjects
}

fun processModuleProperty(key: String, path: String, rootProjects: MutableMap<String, MutableList<String>>) {

    val moduleName = key.substring("module.".length)
    val moduleFile = File(path)

    if (moduleFile.exists()) {

        val rootDir = moduleFile.parentFile.absolutePath
        rootProjects.getOrPut(rootDir) { mutableListOf() }.add(moduleName)
    }
}

fun configureIncludeBuild(rootDir: String, modules: List<String>) {

    val repoName = File(rootDir).name
    includeBuild(rootDir) {

        dependencySubstitution {

            substituteModules(modules, repoName, this)
        }
    }
}

fun substituteModules(modules: List<String>, repoName: String, substitution: DependencySubstitutions) {

    modules.forEach { name ->

        // Xác định group dựa trên project (group theo tên Repo)
        val group = if (repoName == "ComponentService") {

            "com.github.hoanganhtuan95ptit"
        } else {

            "com.github.hoanganhtuan95ptit.$repoName"
        }

        // Xác định artifactName (riêng ComponentService có artifactName là ComponentService)
        val artifactName = if (repoName == "ComponentService" && name == "component-service") {

            "ComponentService"
        } else {

            name
        }

        substitution.substitute(substitution.module("${group}:${artifactName}")).using(substitution.project(":${name}"))
    }
}
