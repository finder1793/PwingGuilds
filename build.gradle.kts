plugins {
    `java-library`
    id("com.gradleup.shadow") version "9.0.0-beta4"
}

group = "com.pwing"
version = "1.0.0"

repositories {
    mavenCentral()
    maven {
        name = "spigot-repo"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "lumine"
        url = uri("https://mvn.lumine.io/repository/maven-public/")
    }
    maven {
        name = "placeholderapi"
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
    maven {
        name = "jitpack"
        url = uri("https://jitpack.io")
    }
    maven {
        name = "enginehub"
        url = uri("https://maven.enginehub.org/repo/")
    }
    // Add Skript repository
    maven {
        name = "skript-releases"
        url = uri("https://repo.skriptlang.org/releases")
    }
}

dependencies {
    compileOnly(libs.spigot.api)
    compileOnly(libs.paper.api)
    compileOnly(libs.mythic.mobs)
    compileOnly(libs.placeholderapi)
    implementation(libs.hikaricp)
    compileOnly(libs.vault)
    compileOnly(libs.adventure.api)
    compileOnly(libs.adventure.text.minimessage)
    compileOnly(libs.adventure.text.serializer.legacy)
    compileOnly(libs.worldguard)
    compileOnly(libs.worldedit)
    implementation("com.zaxxer:HikariCP:5.1.0") {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    // Add Skript dependency
    compileOnly(libs.skript) {
        exclude(group = "com.sk89q.worldguard", module = "worldguard-bukkit")
        exclude(group = "net.milkbowl.vault", module = "Vault")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21)) 
    }
    withSourcesJar()
    withJavadocJar()
}

tasks {
    // Temporary jars that won't be kept in final output
    val paperJar by registering(Jar::class) {
        archiveClassifier.set("paper-temp")
        from(sourceSets.main.get().output)
        from("src/main/resources") {
            include("paper-plugin.yml")
            include("plugin.yml")
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }
        // Don't copy to build/libs
        destinationDirectory.set(layout.buildDirectory.dir("tmp"))
    }

    val spigotJar by registering(Jar::class) {
        archiveClassifier.set("spigot-temp")
        from(sourceSets.main.get().output)
        from("src/main/resources") {
            include("plugin.yml")
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }
        // Don't copy to build/libs
        destinationDirectory.set(layout.buildDirectory.dir("tmp"))
    }

    shadowJar {
        dependsOn(paperJar, spigotJar)
        
        // Paper variant
        archiveClassifier.set("paper")
        from(paperJar.get().outputs)
        dependencies {
            include(dependency("com.zaxxer:HikariCP:.*"))
        }
        relocate("com.zaxxer.hikari", "com.pwing.guilds.libs.hikari")
        minimize()
        destinationDirectory.set(layout.buildDirectory.dir("libs"))
        archiveFileName.set("${project.name}-${project.version}-paper.jar")
        
        doLast {
            // Create Spigot variant
            copy {
                from(paperJar.get().outputs)
                into(layout.buildDirectory.dir("libs"))
                rename { "${project.name}-${project.version}-spigot.jar" }
                filter { line ->
                    if (line.contains("paper-plugin.yml")) "" else line
                }
            }
            
            // Clean up temporary files
            delete(layout.buildDirectory.dir("tmp"))
        }
    }

    build {
        dependsOn(shadowJar)
    }

    // Disable the default jar task
    jar {
        enabled = false
    }

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    processResources {
        val props = mapOf(
            "version" to version,
            "apiVersion" to "1.20"
        )
        
        filesMatching(listOf("plugin.yml", "paper-plugin.yml")) {
            expand(props)
        }
    }
}