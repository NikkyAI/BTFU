import moe.nikky.counter.CounterExtension
import net.fabricmc.loom.task.RemapJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import plugin.generateconstants.GenerateConstantsTask

plugins {
    idea
    id("constantsGenerator")
    id("moe.nikky.persistentCounter") version "0.0.5"
    kotlin("jvm") version Kotlin.version
    id("fabric-loom") version Fabric.Loom.version
}

idea {
    module {
        excludeDirs.add(file("run"))
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

base {
    archivesBaseName = Constants.modid
}

val major = Constants.major
val minor = Constants.minor
val patch = Constants.patch

counter {
    variable(id = "buildnumber", key = "$major.$minor.$patch-${Env.branch}") {
        default = 1
    }
}

val counter: CounterExtension = extensions.getByType()

val buildnumber by counter.map

group = Constants.group
description = Constants.description
version = "$major.$minor.$patch-$buildnumber-${Env.branch}"

minecraft {
}

val folder = listOf("btfubackup")
configure<ConstantsExtension> {
    constantsObject(
        pkg = folder.joinToString("."),
        className = project.name
            .split("-")
            .joinToString("") {
                it.capitalize()
            } + "Constants"
    ) {
        field("BUILD_NUMBER") value buildnumber
        field("JENKINS_BUILD_NUMBER") value Env.buildNumber
        field("BUILD") value Env.versionSuffix
        field("MAJOR_VERSION") value major
        field("MINOR_VERSION") value minor
        field("PATCH_VERSION") value patch
        field("VERSION") value "$major.$minor.$patch"
        field("FULL_VERSION") value "$major.$minor.$patch-${Env.versionSuffix}"
        field("MC_VERSION") value "1.14"
        field("FABRIC_API_VERSION") value Fabric.FabricAPI.version
    }
}

val generateConstants by tasks.getting(GenerateConstantsTask::class) {
    kotlin.sourceSets["main"].kotlin.srcDir(outputFolder)
}

// TODO depend on kotlin tasks in the plugin
tasks.withType<KotlinCompile> {
    dependsOn(generateConstants)
}

repositories {
    //    mavenLocal()
    maven(url = "https://maven.fabricmc.net") {
        name = "fabricmc"
    }
    maven(url = "https://kotlin.bintray.com/kotlinx") {
        name = "kotlinx"
    }
    maven(url = "https://jitpack.io") {
        name = "jitpack"
    }
    maven(url = "https://repo.elytradev.com") {
        name = "elytradev"
    }
//    mavenCentral()
    jcenter()
}

configurations.runtimeOnly.extendsFrom(configurations.modCompile)

dependencies {
    minecraft(group = "com.mojang", name = "minecraft", version = Minecraft.version)

    mappings(group = "net.fabricmc", name = "yarn", version = "${Minecraft.version}.${Fabric.Yarn.version}")

    modCompile(group = "net.fabricmc", name = "fabric-loader", version = Fabric.version)

    modCompile(group = "net.fabricmc", name = "fabric-language-kotlin", version = Fabric.LanguageKotlin.version)

    modCompile(group = "net.fabricmc", name = "fabric", version = Fabric.FabricAPI.version + ".+")
}

tasks.getByName<ProcessResources>("processResources") {
    filesMatching("fabric.mod.json") {
        expand(
            mutableMapOf(
                "modid" to Constants.modid,
                "modname" to Constants.name,
                "version" to version,
                "fabricApi" to Fabric.FabricAPI.version,
                "fabricKotlin" to Fabric.LanguageKotlin.version
            )
        )
    }
}
