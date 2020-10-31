object Minecraft {
    const val version = "18w50a"
}

object Kotlin {
    const val version = "1.3.10"
}

object KotlinX {
    object Serialization {
        const val version = "0.9.1"
    }
}

object Fabric {
    const val version = "0.2.0.+"
    object Loom {
        const val version = "0.1.0-SNAPSHOT"
    }
    object Yarn {
        const val version = "+"
    }
    object FabricAPI {
        const val version = "0.1.1"
    }
    object LanguageKotlin {
        const val version = Kotlin.version + "-26"
    }
}
