plugins {
    kotlin("jvm") version "1.4.31"

    id("com.github.johnrengelman.shadow") version "6.1.0"
    `maven-publish`
    java
}

group = "com.redefantasy"
version = "0.1-ALPHA"

repositories {
    mavenCentral()

    mavenLocal()

    jcenter()
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    shadowJar {
        val fileName = "${project.name}.jar"

        archiveFileName.set("${project.name}.jar")

        doLast {
            try {
                val file = file("build/libs/$fileName")

                val toDelete = file("/home/cloud/output/$fileName")

                if (toDelete.exists()) toDelete.delete()

                file.copyTo(file("/home/cloud/output/$fileName"))
                file.delete()
            } catch (ex: java.io.FileNotFoundException) {
                ex.printStackTrace()
            }
        }
    }
}

dependencies {
    // kotlin
    compileOnly(kotlin("stdlib"))

    // paperspigot
    compileOnly("org.github.paperspigot:paperspigot:1.8.8-R0.1-SNAPSHOT")

    // waterfall chat
    compileOnly("io.github.waterfallmc:waterfall-chat:1.16-R0.5-SNAPSHOT")

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
    compileOnly("com.redefantasy:core-shared:0.1-ALPHA")
    compileOnly("com.redefantasy:core-spigot:0.1-ALPHA")

    // factions
    compileOnly("com.massivecraft:massivecore:2.13.6")
    compileOnly("com.massivecraft:factions:2.13.6")
    // factions
}

val sources by tasks.registering(Jar::class) {
    archiveFileName.set(project.name)
    archiveClassifier.set("sources")
    archiveVersion.set(null as String?)

    from(sourceSets.main.get().allSource)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["kotlin"])
            artifact(sources.get())
        }
    }
}