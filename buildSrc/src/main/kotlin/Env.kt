object Env {
    val branch = System.getenv("GIT_BRANCH") ?: "local"
        .takeUnless { it == "master" }
        ?: ""

    val versionSuffix = System.getenv("BUILD_NUMBER") ?: "local"

    val buildNumber = System.getenv("BUILD_NUMBER")?.toIntOrNull() ?: -1
}