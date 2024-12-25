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
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly("io.lumine:Mythic-Dist:5.3.5") // MythicMobs dependency
    implementation("com.zaxxer:HikariCP:5.0.1")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
    }
    
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(17)
    }
}