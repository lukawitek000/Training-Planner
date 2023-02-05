plugins {
    id(BuildPlugins.commonLibraryPlugin)
}

android {
    namespace = "com.lukasz.witkowski.training.planner.session.service"
}

dependencies {
    implementation(projects.statistics)
    implementation(projects.training)
}
