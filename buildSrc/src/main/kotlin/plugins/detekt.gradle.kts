package plugins

import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.kotlin.dsl.register

plugins {
    id("io.gitlab.arturbosch.detekt")
}

detekt {
    config.from("$rootDir/config/detekt/detekt.yml")
    description = "CUSTOM Detekt check"
    parallel = true
    buildUponDefaultConfig = false
    source.from("src/main/kotlin", "src/main/java")
}

tasks.named<Detekt>("detekt").configure {
    reports {
        html.required.set(true)
        xml.required.set(false)
        txt.required.set(false)
    }
}
