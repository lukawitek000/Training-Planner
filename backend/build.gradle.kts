plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}

application {
    mainClass.set("com.lukasz.witkowski.training.planner.backend.ApplicationKt")
}

dependencies {
    implementation(project(":dto"))

    // Ktor Server
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.contentNegotiation)
    implementation(libs.ktor.serialization.kotlinxJson)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.authJwt)
    implementation(libs.ktor.server.statusPages)
    implementation(libs.ktor.server.websockets)
    implementation(libs.ktor.server.callLogging)

    // Database & Security
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.hikaricp)
    implementation(libs.h2.database)
    implementation(libs.postgresql)
    implementation(libs.bcrypt)
    implementation(libs.logback)

    // Tests & Test Client
    testImplementation(libs.ktor.server.testHost)
    testImplementation("io.ktor:ktor-client-core-jvm:${libs.versions.ktor.get()}")
    testImplementation("io.ktor:ktor-client-content-negotiation-jvm:${libs.versions.ktor.get()}")
    testImplementation("io.ktor:ktor-serialization-kotlinx-json-jvm:${libs.versions.ktor.get()}")
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit)
}
