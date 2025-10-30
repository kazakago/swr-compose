plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.kotlinPlugin)
    implementation(libs.dokkaPlugin)
    implementation(libs.mavenPublishPlugin)
}
