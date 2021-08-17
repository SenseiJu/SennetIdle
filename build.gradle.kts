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
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:+")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:+")
    implementation("org.jetbrains.kotlin:kotlin-reflect:+")
    implementation("com.github.SenseiJu:Sentils:3715b60a07")
    implementation("dev.triumphteam:triumph-gui:+")
    implementation("me.mattstudios.utils:matt-framework:1.4")
    implementation("com.zaxxer:HikariCP:+")
    implementation("org.ktorm:ktorm-core:+")
    implementation("org.ktorm:ktorm-support-mysql:+")

    compileOnly("io.papermc.paper:paper-api:+")
    compileOnly("com.comphenix.protocol:ProtocolLib:+")
    compileOnly("com.gmail.filoghost.holographicdisplays:holographicdisplays-api:+")

    testImplementation("org.junit.jupiter:junit-jupiter-api:+")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:+")
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