pluginManagement {
    repositories {
        maven(url="http://maven.fabricmc.net/") {
            name = "FabricMC"
        }
        jcenter()
        gradlePluginPortal()
    }
}
rootProject.name = Constants.name
include("core")