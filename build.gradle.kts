plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.pwing"
version = "1.0.1"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://mvn.lumine.io/repository/maven-public/") // MythicMobs repo
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.infernalsuite.com/repository/maven-snapshots/")
    maven("https://repo.rapture.pw/repository/maven-releases/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly("io.lumine:Mythic-Dist:5.3.5")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.infernalsuite.aswm:api:3.0.0-SNAPSHOT")
    implementation("com.zaxxer:HikariCP:5.0.1")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("net.kyori:adventure-api:4.14.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.14.0")
}

configurations.all {
    resolutionStrategy {
        force("com.google.guava:guava:32.1.3-jre")
        force("org.yaml:snakeyaml:2.2")
    }
}

tasks {
    shadowJar {
        archiveClassifier.set("")
    }

    compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    processResources {
        filesMatching("plugin.yml") {
            expand(project.properties)
        }
    }
}
