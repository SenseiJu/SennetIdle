pluginManagement {
    plugins {
        kotlin("jvm") version "2.2.20"
        kotlin("plugin.serialization") version "2.2.20"
        id("com.gradleup.shadow") version "9.2.2"
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "SennetIdle"
