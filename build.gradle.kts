plugins {
    kotlin("jvm") version "2.2.21"
    id("com.google.cloud.tools.jib") version "3.5.2"
}

group = "me.centralhardware"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

val ktgbotapiVersion = "30.0.2"

dependencies {
    // Telegram Bot API
    implementation("dev.inmo:tgbotapi:${ktgbotapiVersion}")
    implementation("com.github.centralhardware:ktgbotapi-commons:${ktgbotapiVersion}")
    implementation("com.github.centralhardware.ktgbotapi-middlewars:ktgbotapi-restrict-access-middleware:${ktgbotapiVersion}")
}

jib {
    from {
        image = System.getenv("JIB_FROM_IMAGE") ?: "eclipse-temurin:24-jre"
    }
    to {
    }
    container {
        mainClass = "MainKt"
        jvmFlags = listOf("-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0")
        creationTime = "USE_CURRENT_TIMESTAMP"
        labels = mapOf(
            "org.opencontainers.image.source" to (System.getenv("GITHUB_SERVER_URL")?.let { server ->
                val repo = System.getenv("GITHUB_REPOSITORY")
                if (repo != null) "$server/$repo" else ""
            } ?: ""),
            "org.opencontainers.image.revision" to (System.getenv("GITHUB_SHA") ?: "")
        )
        user = "10001"
    }
}


tasks.test {
    useJUnitPlatform()
}
