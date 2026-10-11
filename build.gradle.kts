import xyz.jpenilla.resourcefactory.paper.PaperPluginYaml

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.shadow)
    alias(libs.plugins.resourceFactoryPaperConvention)
}

group = "gg.idlemob"
version = System.getenv("GITHUB_REF_NAME")?.removePrefix("v") ?: "local-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://repo.xenondevs.xyz/releases")
    maven("https://repo.fancyinnovations.com/releases")
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    // Infra
    implementation(libs.hikari)
    implementation(libs.mariadbJdbc)
    implementation(libs.exposedCore)
    implementation(libs.exposedJdbc)
    implementation(libs.exposedJavaTime)
    implementation(libs.coroutines)

    // Plugin APIs
    compileOnly(libs.paperApi)
    compileOnly(libs.placeholderApi)
    compileOnly(libs.fancyHolograms)
    compileOnly(libs.fancyNpcs)
    compileOnly(libs.worldGuard)

    // Libraries
    implementation(libs.acfPaper)
    implementation(libs.invui)
    implementation(libs.configurateYaml)
}

configurations.runtimeClasspath {
    exclude(group = "org.slf4j", module = "slf4j-api")
    exclude(group = "org.jetbrains", module = "annotations")
    exclude(group = "org.jspecify", module = "jspecify")
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        javaParameters.set(true)
    }
}

paperPluginYaml {
    authors = listOf("SenseiJu")
    apiVersion = "26.3"
    main = "gg.idlemob.idlemobcore.IdleMobCore"
    dependencies {
        server("PlaceholderAPI", PaperPluginYaml.Load.BEFORE, required = true)
        server("FancyHolograms", PaperPluginYaml.Load.BEFORE, required = true)
        server("FancyNpcs", PaperPluginYaml.Load.BEFORE, required = true)
        server("WorldGuard", PaperPluginYaml.Load.BEFORE, required = true)
    }
}

tasks {
    build {
        dependsOn("shadowJar")
    }

    shadowJar {
        archiveClassifier.set("")
        // Needed so mergeServiceFiles sees every service file
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        val shadePath = "gg.idlemob.idlemobcore.shaded"

        relocate("co.aikar", "$shadePath.acf")
        relocate("org.spongepowered.configurate", "$shadePath.configurate")
        relocate("io.leangen.geantyref", "$shadePath.geantyref")
        relocate("net.kyori.option", "$shadePath.kyori.option")
        relocate("com.zaxxer.hikari", "$shadePath.hikari")
        relocate("org.mariadb.jdbc", "$shadePath.mariadb")
        relocate("org.jetbrains.exposed", "$shadePath.exposed")
        relocate("xyz.xenondevs", "$shadePath.invui")

        mergeServiceFiles()
    }
}
