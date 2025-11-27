plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.kazakago.swr.example.androidapp"
    compileSdk = libs.versions.androidCompileSdk.get().toInt()
    defaultConfig {
        applicationId = "com.kazakago.swr.example.androidapp"
        minSdk = libs.versions.androidMinSdk.get().toInt()
        targetSdk = libs.versions.androidTargetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility(libs.versions.jvmToolchain.get())
        targetCompatibility(libs.versions.jvmToolchain.get())
    }
}

dependencies {
    implementation(projects.exampleApp)
}
