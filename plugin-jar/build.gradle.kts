plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

dependencies {
    implementation(project(":plugin-interface"))
    implementation(compose.desktop.common)
}
