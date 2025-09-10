import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.android.library)
    alias(libs.plugins.buildlogic.publish)
}

kotlin {
    explicitApi()

    androidLibrary {
        namespace = "com.kazakago.swr.runtime"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        withHostTestBuilder {}.configure {}
        compilations.configureEach {
            compilerOptions.configure {
                jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvmToolchain.get()))
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    jvm()
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.swrStore)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.androidx.lifecycle.runtime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.androidx.lifecycle.runtime.testing)
            implementation(libs.turbine)
        }
        androidMain.dependencies {
            implementation(libs.androidx.startup)
        }
        jvmMain.dependencies {
            implementation(libs.kotlinx.coroutines.swing)
        }
        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
    }
}
