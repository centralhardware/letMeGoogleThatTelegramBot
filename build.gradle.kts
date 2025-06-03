import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.1.21"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "me.centralhardware"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("dev.inmo:tgbotapi:25.0.0")
    implementation("com.github.centralhardware:ktgbotapi-commons:6ef1dde4fe")
    implementation("com.github.seratch:kotliquery:1.9.1")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("MainKt")
}

