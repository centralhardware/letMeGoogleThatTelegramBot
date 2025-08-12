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

val ktgbotapiVersion = "28.0.0"

dependencies {
    implementation("dev.inmo:tgbotapi:$ktgbotapiVersion")
    implementation("com.github.centralhardware:ktgbotapi-commons:$ktgbotapiVersion-1")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("MainKt")
}

