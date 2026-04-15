pluginManagement {
    repositories {
        google()
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
        
        // Load local.properties to read Mapbox token
        val localProperties = java.util.Properties()
        val localPropertiesFile = settingsDir.resolve("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { localProperties.load(it) }
        }
        
        val mapboxToken = localProperties.getProperty("MAPBOX_DOWNLOADS_TOKEN")
            ?: providers.gradleProperty("MAPBOX_DOWNLOADS_TOKEN").getOrNull()
            ?: System.getenv("MAPBOX_DOWNLOADS_TOKEN")
            ?: ""

        if (mapboxToken.isNotEmpty()) {
            maven {
                url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
                authentication {
                    create<BasicAuthentication>("basic")
                }
                credentials {
                    username = "mapbox"
                    password = mapboxToken.trim()
                }
            }
        }
    }
}

rootProject.name = "Mind Access"
include(":app")
