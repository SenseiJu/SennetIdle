import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "+"
    kotlin("plugin.serialization") version "+"

    id("com.github.johnrengelman.shadow") version "+"
}

group = "me.senseiju"
version = "0.0.1"

repositories {
    mavenCentral()

    maven("https://jitpack.io")
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.mattstudios.me/artifactory/public/")
    maven("https://repo.panda-lang.org/releases")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.31")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2-native-mt")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.31")
    implementation("com.github.SenseiJu:Sentils:3715b60a07")
    implementation("dev.triumphteam:triumph-gui:+")
    implementation("me.mattstudios.utils:matt-framework:1.4.6")
    implementation("com.zaxxer:HikariCP:5.0.0")

    compileOnly("io.papermc.paper:paper-api:+")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.7.0")
    compileOnly("me.clip:placeholderapi:+")
    compileOnly("com.gmail.filoghost.holographicdisplays:holographicdisplays-api:+")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
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

        minimize {
            exclude(dependency("org.jetbrains.kotlin:kotlin-reflect:.*"))
        }
    }

    processResources {
        filesMatching("**/plugin.yml") { expand("version" to project.version) }
    }

    test {
        useJUnitPlatform()
    }
}