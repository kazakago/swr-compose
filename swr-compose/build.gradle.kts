import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.library)
    alias(libs.plugins.buildlogic.publish)
}

kotlin {
    explicitApi()

    @Suppress("UnstableApiUsage")
    androidLibrary {
        namespace = "com.kazakago.swr.compose"
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
            api(projects.swrRuntime)
            implementation(compose.ui)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.androidx.lifecycle.runtime.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
