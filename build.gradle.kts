plugins {
    `java-library`
    id("com.gradleup.shadow") version "9.0.0-beta4"
}

group = "com.pwing"
version = "1.0.1"

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
    maven {
        name = "skript-releases"
        url = uri("https://repo.skriptlang.org/releases")
    }
    maven {
        name = "viaversion"
        url = uri("https://repo.viaversion.com")
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
    compileOnly(libs.skript) {
        exclude(group = "com.sk89q.worldguard", module = "worldguard-bukkit")
        exclude(group = "net.milkbowl.vault", module = "Vault")
    }
    compileOnly(libs.viaversion)
    implementation("com.sk89q.worldedit:worldedit-bukkit:7.2.9") {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    implementation("com.zaxxer:HikariCP:5.1.0") {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    compileOnly(files("libs/PwingEco-1.1.2.jar"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
    withJavadocJar()
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        dependencies {
            include(dependency("com.zaxxer:HikariCP:.*"))
        }
        relocate("com.zaxxer.hikari", "com.pwing.guilds.libs.hikari")
        minimize()
        destinationDirectory.set(layout.buildDirectory.dir("libs"))
        archiveFileName.set("${project.name}-${project.version}.jar")
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
        
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}