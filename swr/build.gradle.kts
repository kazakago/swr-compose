import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.dokka)
    alias(libs.plugins.android.library)
    alias(libs.plugins.maven.publish)
}

kotlin {
    explicitApi()
    @Suppress("UnstableApiUsage")
    androidLibrary {
        namespace = "com.kazakago.swr.compose"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
        withHostTestBuilder {}.configure {}
        compilations.configureEach {
            compilerOptions.configure {
                jvmTarget.set(JvmTarget.JVM_1_8)
            }
        }
    }
    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        commonMain.dependencies {
            implementation(compose.ui)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.konnection)
        }
        jvmMain.dependencies {
            implementation(libs.kotlinx.coroutines.swing)
        }
        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
        }
        androidUnitTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.androidx.compose.ui.test.junit4)
            implementation(libs.androidx.compose.ui.test.manifest)
            implementation(libs.robolectric)
            implementation(libs.mockk)
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates(
        groupId = "com.kazakago.swr.compose",
        version = libs.versions.version.get(),
    )
    pom {
        name.set("swr-compose")
        description.set("\"React SWR\" ported for Jetpack Compose & Compose Multiplatform")
        inceptionYear.set("2022")
        url.set("https://github.com/kazakago/swr-compose")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("kazakago")
                email.set("kazakago@gmail.com")
                url.set("https://github.com/kazakago/")
            }
        }
        scm {
            url.set("https://github.com/kazakago/swr-compose")
            connection.set("scm:git:git://github.com/kazakago/swr-compose.git")
            developerConnection.set("scm:git:ssh://git@github.com/kazakago/swr-compose.git")
        }
    }
}
