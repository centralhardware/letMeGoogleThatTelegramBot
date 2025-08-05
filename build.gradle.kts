plugins {
    kotlin("jvm") version "2.2.0"
    application
}

group = "me.centralhardware"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("dev.inmo:tgbotapi:27.1.1")
    implementation("com.github.centralhardware:ktgbotapi-commons:beafbfc9a8")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("MainKt")
}

