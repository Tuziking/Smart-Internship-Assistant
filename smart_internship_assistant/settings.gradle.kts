pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven( url = uri("https://jitpack.io") )
        maven( url = uri("https://maven.aliyun.com/repository/public"))
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Smart_Internship_Assistant"
include(":app")
 