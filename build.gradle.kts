plugins {
    //`java-library`
    java
    val kotlinVersion = "1.8.0"
    kotlin("jvm") version kotlinVersion
    //kotlin("plugin.serialization") version kotlinVersion
    id("com.github.johnrengelman.shadow") version "7.1.+"
    // lombok is not needed from kotlin, we have even more features this way
}

group "dev.kosmx.geyserEmotes"
version "0.1-SNAPSHOT"

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    /*
    maven{
        name = "bundabrg-repo"
        url = "https://repo.worldguard.com.au/repository/maven-public"
    }*/
    maven("https://jitpack.io")

    maven("https://repo.opencollab.dev/maven-releases/") {
        name = "geyser"
        mavenContent {
            releasesOnly()
        }
        metadataSources { ignoreGradleMetadataRedirection() }
    }
    maven("https://repo.opencollab.dev/maven-snapshots/") {
        name = "geyser-snapshots"
        mavenContent {
            //snapshotsOnly()
        }
        //metadataSources { ignoreGradleMetadataRedirection() }
    }
}

configurations {
    create("shadowImplementation")
    compileClasspath.get().extendsFrom(this["shadowImplementation"])
    runtimeClasspath.get().extendsFrom(this["shadowImplementation"])
}

dependencies {

    // maven module is kinda broken, gradle module "works?!"
    implementation(module("org.geysermc.geyser:core:${project.property("geyser_plugin")}"))
    "shadowImplementation"("org.jetbrains.kotlin:kotlin-stdlib:1.8.10")
}

tasks {
    withType<JavaCompile>().configureEach {
        options.release.set(java.targetCompatibility.majorVersion.toInt())
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = java.targetCompatibility.toString()
    }

    jar {
        from("LICENSE") {
            rename { "${it}_${base.archivesName.get()}" }
        }
        archiveClassifier.set("slim")
    }

    shadowJar {
        configurations = listOf(project.configurations["shadowImplementation"])
        relocate("kotlin", "dev.kosmx.geyserEmotes.kotlin")

        archiveClassifier.set("")
    }
    build {
        dependsOn(shadowJar)
    }
}
