plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.pwing"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://mvn.lumine.io/repository/maven-public/") // MythicMobs repo
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI repo
    maven("https://repo.infernalsuite.com/repository/maven-snapshots/")//aswm repo
    maven("https://repo.rapture.pw/repository/maven-releases/")//aswm repo
    maven("https://jitpack.io")
}
dependencies {
    compileOnly("org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly("io.lumine:Mythic-Dist:5.3.5") // MythicMobs dependency
    compileOnly("me.clip:placeholderapi:2.11.6") // PlaceholderAPI dependency
    compileOnly("com.infernalsuite.aswm:api:3.0.0-SNAPSHOT") // ASWM dependency
    implementation("com.zaxxer:HikariCP:5.0.1")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
    }

    compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
}
