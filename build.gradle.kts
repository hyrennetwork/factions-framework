plugins {
    kotlin("jvm") version "1.4.31"

    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("maven-publish")
    id("java")
}

group = "com.redefantasy"
version = "0.1-ALPHA"

repositories {
    mavenCentral()

    mavenLocal()

    jcenter()
}

sourceSets {
    main {
        java {
            setSrcDirs(
                listOf("src/main/kotlin")
            )
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

sourceSets {
    main {
        java.srcDirs("main/kotlin")
    }
}

dependencies {
    // kotlin
    compileOnly(kotlin("stdlib"))

    // paperspigot
    compileOnly("org.github.paperspigot:paperspigot:1.8.8-R0.1-SNAPSHOT")

    // waterfall chat
    compileOnly("io.github.waterfallmc:waterfall-chat:1.16-R0.5-SNAPSHOT")

    // eventbus
    compileOnly("org.greenrobot:eventbus:3.2.0")

    // caffeine
    compileOnly("com.github.ben-manes.caffeine:caffeine:2.8.5")

    // core
    compileOnly("com.redefantasy:core-shared:0.1-ALPHA")
    compileOnly("com.redefantasy:core-spigot:0.1-ALPHA")
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