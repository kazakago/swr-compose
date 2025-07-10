plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.kotlin.plugin)
    implementation(libs.dokka.plugin)
    implementation(libs.maven.publish.plugin)
}
