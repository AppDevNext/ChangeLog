plugins {
    id("com.android.library")
    id("kotlin-android")
    id("maven-publish")
    kotlin("plugin.serialization") version "2.2.20"
}

android {
    namespace = "info.hannes.changeloglib"
    defaultConfig {

        minSdk = 21
        compileSdk = 35
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
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.2.20")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    api("com.google.android.material:material:1.13.0")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["release"])
                pom {
                    licenses {
                        license {
                            name = "Apache License Version 2.0"
                            url = "https://github.com/AppDevNext/ChangeLog/blob/master/LICENSE"
                        }
                    }
                }
            }
        }
    }
}