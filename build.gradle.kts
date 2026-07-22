import org.gradle.api.attributes.java.TargetJvmVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")

    id("com.gradleup.shadow")
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
    maven("https://repo.alessiodp.com/releases/")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("com.github.SenseiJu:Sentils:-SNAPSHOT")
    implementation("dev.triumphteam:triumph-gui:3.1.3")
    implementation("me.mattstudios.utils:matt-framework:1.4.6")
    implementation("com.zaxxer:HikariCP:5.1.0")

    compileOnly("io.papermc.paper:paper-api:26.1.2.build.72-stable")
    compileOnly("net.kyori:adventure-text-minimessage:4.18.0")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0")
    compileOnly("me.clip:placeholderapi:2.12.2")
    compileOnly("com.github.decentsoftware-eu:decentholograms:2.8.12")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("net.citizensnpcs:citizens-main:2.0.42-SNAPSHOT")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.0")
}

// Paper 26.1.2 is built for Java 25, so we compile against it with a JDK 25 toolchain.
// The Kotlin 2.2.20 compiler only emits up to JVM 24 bytecode, which still runs fine on
// the Java 25 runtime, so we pin the Kotlin/Java compile targets to 24 to keep them consistent.
kotlin {
    jvmToolchain(25)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_24)
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(24)
}

// paper-api 26.1.2 declares a minimum runtime of Java 25 (which this plugin does run on, since
// Paper 26.1.2 requires it). We emit Java 24 bytecode, so tell Gradle's variant resolution that
// our runtime target is 25 so the dependency resolves.
configurations.named("compileClasspath") {
    attributes {
        attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 25)
    }
}

tasks {
    shadowJar {
        archiveFileName.set("SennetIdle-${project.version}.jar")

        relocate("dev.triumphteam.gui", "me.senseiju.shaded.gui")
        relocate("me.mattstudios.mf", "me.senseiju.shaded.cmd")

        minimize()
    }

    build {
        dependsOn(shadowJar)
    }

    processResources {
        filesMatching("**/plugin.yml") { expand("version" to project.version) }
    }

    test {
        useJUnitPlatform()
    }
}
