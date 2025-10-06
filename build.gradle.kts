import org.gradle.internal.jvm.Jvm

buildscript {
    repositories {
        mavenCentral()
        google()
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.13.0")
        classpath("com.github.dcendents:android-maven-gradle-plugin:2.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.20")
    }
}

println("Gradle uses Java ${Jvm.current()}")

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
