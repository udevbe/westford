object depversion {
    val kotlin = "1.1.2-2"
    val wayland_java_bindings = "1.5.2-SNAPSHOT"
    val jaccall = "2.0.0-SNAPSHOT"
    val auto_factory = "1.0-beta5"
    val auto_value = "1.4.1"
    val dagger = "2.11-rc1"
}

plugins {
    id("org.jetbrains.kotlin.kapt") version "1.1.2-2"
    id("org.jetbrains.kotlin.jvm") version "1.1.2-2"
}

kapt {
    correctErrorTypes = true
}

apply {
    plugin("kotlin")
    plugin("kotlin-kapt")
}

buildscript {
    repositories {
        gradleScriptKotlin()
    }
    dependencies {
        classpath(kotlinModule("gradle-plugin"))
    }
}

dependencies {
    repositories {
        mavenCentral()
        mavenLocal()
    }

    //kotlin
    compile(kotlinModule("stdlib",
                         depversion.kotlin))

    //wayland
    compile("org.freedesktop:wayland-server:${depversion.wayland_java_bindings}")

    //jaccall
    kapt("org.freedesktop:jaccall.generator:${depversion.jaccall}")

    //auto
    compile("com.google.auto.factory:auto-factory:${depversion.auto_factory}")
    kapt("com.google.auto.factory:auto-factory:${depversion.auto_factory}")
    compile("com.google.auto.value:auto-value:${depversion.auto_value}")
    kapt("com.google.auto.value:auto-value:${depversion.auto_value}")

    //dagger
    compile("com.google.dagger:dagger:${depversion.dagger}")
    kapt("com.google.dagger:dagger-compiler:${depversion.dagger}")

    //test
    testCompile("org.jetbrains.kotlin:kotlin-test-junit:${depversion.kotlin}")
}



