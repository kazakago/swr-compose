import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
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
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
        publishLibraryVariants("release")
    }
    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
    sourceSets {
        commonMain.dependencies {
            implementation(compose.ui)
            implementation(libs.androidx.lifecycle.runtime.compose)
        }
        jvmMain.dependencies {
            implementation(libs.kotlinx.coroutines.swing)
        }
        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.androidx.startup)
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

android {
    namespace = "com.kazakago.swr.compose"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates(
        groupId = "com.kazakago.swr.compose",
        version = "0.7.2",
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
