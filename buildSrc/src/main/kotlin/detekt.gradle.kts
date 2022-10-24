import io.gitlab.arturbosch.detekt.Detekt

tasks.register<Detekt>("customDetekt") {
    val configFile = files("$rootDir/config/detekt/detekt.yml")
    description = "Custom DETEKT build for all modules"
    parallel = true
    buildUponDefaultConfig = false
    setSource(files("src/main/kotlin", "src/main/java"))
    config.setFrom(configFile)
    reports {
        html.required.set(true)
        xml.required.set(false)
        txt.required.set(false)
    }
}
