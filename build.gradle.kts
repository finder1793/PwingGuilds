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
        minimize()
        relocate("com.zaxxer.hikari", "com.pwing.guilds.libs.hikari")
        dependencies {
            include(dependency("com.zaxxer:HikariCP:.*"))
        }
    }

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(21) 
    }

    processResources {
        filesMatching("plugin.yml") {
            expand(project.properties)
        }
        filesMatching("paper-plugin.yml") {
            expand(project.properties)
        }
    }
}
