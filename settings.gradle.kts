pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "Recall"

// App entry point
include(":app")

// Core modules (bottom of dependency graph)
include(":core:common")
include(":core:domain")
include(":core:data")
include(":core:prefs")
include(":core:ui")
include(":core:ai-engine")

// Feature modules
include(":feature:notes")
include(":feature:ai")
include(":feature:reminders")
include(":feature:onboarding")
include(":feature:settings")
include(":feature:widget")
