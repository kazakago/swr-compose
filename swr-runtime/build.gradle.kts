import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.buildLogicPublish)
}

kotlin {
    explicitApi()

    android {
        namespace = "com.kazakago.swr.runtime"
        compileSdk = libs.versions.androidCompileSdk.get().toInt()
        minSdk = libs.versions.androidMinSdk.get().toInt()
        withHostTest {}
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvmToolchain.get()))
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
    js {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.swrStore)
            implementation(libs.kotlinxCoroutinesCore)
            implementation(libs.androidxLifecycleRuntime)
        }
        commonTest.dependencies {
            implementation(libs.kotlinTest)
            implementation(libs.kotlinxCoroutinesTest)
            implementation(libs.androidxLifecycleRuntimeTesting)
            implementation(libs.turbine)
        }
        androidMain.dependencies {
            implementation(libs.androidxStartup)
        }
        jvmMain.dependencies {
            implementation(libs.kotlinxCoroutinesSwing)
        }
        webMain.dependencies {
            implementation(libs.kotlinxBrowser)
        }
    }
}
