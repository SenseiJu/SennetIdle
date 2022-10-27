import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"

    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "me.senseiju"
version = "0.3.1"

repositories {
    mavenCentral()

    maven("https://jitpack.io")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.panda-lang.org/releases")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://maven.citizensnpcs.co/repo")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.7.20")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.20")
    implementation("com.github.SenseiJu:Sentils:-SNAPSHOT")
    implementation("dev.triumphteam:triumph-gui:3.1.3")
    implementation("me.mattstudios.utils:matt-framework:1.4.6")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("net.kyori:adventure-text-minimessage:4.11.0")

    compileOnly("io.papermc.paper:paper-api:1.19.2-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.8.0")
    compileOnly("me.clip:placeholderapi:2.11.2")
    compileOnly("com.github.decentsoftware-eu:decentholograms:2.5.2")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("net.citizensnpcs:citizens-main:2.0.30-SNAPSHOT")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
}

tasks {
    register("copyJarToServer", Copy::class) {
        from(shadowJar)
        into("D:/Servers/Minecraft/SennetIdle/plugins/update")
    }

    withType(ShadowJar::class) {
        archiveFileName.set("SennetIdle-${project.version}.jar")

        relocate("dev.triumphteam.gui", "me.senseiju.shaded.gui")
        relocate("me.mattstudios.mf", "me.senseiju.shaded.cmd")

        minimize()
    }

    processResources {
        filesMatching("**/plugin.yml") { expand("version" to project.version) }
    }

    test {
        useJUnitPlatform()
    }
}