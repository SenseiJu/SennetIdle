import xyz.jpenilla.resourcefactory.paper.PaperPluginYaml

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.shadow)
    alias(libs.plugins.resourceFactoryPaperConvention)
}

group = "me.senseiju"
version = System.getenv("GITHUB_REF_NAME")?.removePrefix("v") ?: "local-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://repo.xenondevs.xyz/releases")
    maven("https://repo.fancyinnovations.com/releases")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly(libs.paperApi)
    compileOnly(libs.placeholderApi)
    compileOnly(libs.fancyHolograms)
    compileOnly(libs.fancyNpcs)
    compileOnly(libs.vaultApi) {
        isTransitive = false
    }

    implementation(libs.acfPaper)
    implementation(libs.configurateYaml)
    implementation(libs.configurateGson) {
        exclude(group = "com.google.code.gson", module = "gson")
    }
    implementation(libs.hikari)
    implementation(libs.mariadbJdbc)
    implementation(libs.invui)
}

configurations.runtimeClasspath {
    exclude(group = "org.slf4j", module = "slf4j-api")
    exclude(group = "com.google.errorprone", module = "error_prone_annotations")
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
    name = rootProject.name
    authors = listOf("SenseiJu")
    apiVersion = "26.3"
    main = "me.senseiju.sennetidle.SennetIdle"
    version = project.version.toString()
    dependencies {
        server("PlaceholderAPI", PaperPluginYaml.Load.BEFORE, required = true)
        server("Vault", PaperPluginYaml.Load.BEFORE, required = true)
        server("FancyHolograms", PaperPluginYaml.Load.BEFORE, required = true)
        server("FancyNpcs", PaperPluginYaml.Load.BEFORE, required = true)
    }
}

tasks {
    build {
        dependsOn("shadowJar")
    }

    shadowJar {
        archiveBaseName.set(rootProject.name)
        archiveVersion.set(project.version.toString())
        archiveClassifier.set("")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        val shadePath = "me.senseiju.sennetidle.shaded"

        relocate("co.aikar", "$shadePath.acf")
        relocate("org.spongepowered.configurate", "$shadePath.configurate")
        relocate("org.yaml.snakeyaml", "$shadePath.snakeyaml")
        relocate("io.leangen.geantyref", "$shadePath.geantyref")
        relocate("net.kyori.option", "$shadePath.kyori.option")
        relocate("com.zaxxer.hikari", "$shadePath.hikari")
        relocate("org.mariadb.jdbc", "$shadePath.mariadb")
        relocate("xyz.xenondevs", "$shadePath.invui")

        mergeServiceFiles()
    }
}
