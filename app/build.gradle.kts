plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    namespace = "info.hannes.changelog.sample"
    defaultConfig {
        versionCode = 1
        versionName = "1.0"

        compileSdk = 36
        minSdk = 21
    }
    packaging {
        resources {
            pickFirsts += listOf("META-INF/atomicfu.kotlin_module")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }
}

dependencies {
    implementation(project(":ChangeLogLib"))
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.2.20")
}

