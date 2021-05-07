plugins {
    kotlin("jvm") version "1.4.31"

    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "net.hyren"
version = "0.1-ALPHA"

repositories {
    mavenCentral()

    jcenter()

    maven("https://maven.pkg.github.com/hyrendev/nexus/") {
        credentials {
            username = System.getenv("MAVEN_USERNAME")
            password = System.getenv("MAVEN_PASSWORD")
        }
    }
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    shadowJar {
        archiveFileName.set("${project.name}.jar")
    }
}

dependencies {
    // kotlin
    compileOnly(kotlin("stdlib"))

    // paperspigot
    compileOnly("org.github.paperspigot:paperspigot:1.8.8-R0.1-SNAPSHOT")

    // exposed
    compileOnly("org.jetbrains.exposed:exposed-core:0.29.1")
    compileOnly("org.jetbrains.exposed:exposed-dao:0.29.1")
    compileOnly("org.jetbrains.exposed:exposed-jdbc:0.29.1")
    compileOnly("org.jetbrains.exposed:exposed-jodatime:0.29.1")

    // jackson
    compileOnly("com.fasterxml.jackson.core:jackson-databind:2.12.2")
    compileOnly("com.fasterxml.jackson.core:jackson-annotations:2.12.2")

    // eventbus
    compileOnly("org.greenrobot:eventbus:3.2.0")

    // caffeine
    compileOnly("com.github.ben-manes.caffeine:caffeine:2.8.5")

    // core
    compileOnly("net.hyren:core-shared:0.1-ALPHA")
    compileOnly("net.hyren:core-spigot:0.1-ALPHA")
}