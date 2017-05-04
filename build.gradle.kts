buildscript {

    group = "org.westford"
    version = "1.0-SNAPSHOT"

    repositories {
        gradleScriptKotlin()
        mavenCentral()
        mavenLocal()
    }
}

plugins {
    base
}

dependencies {
    // Make the root project archives configuration depend on every sub-project
    subprojects.forEach {
        archives(it)
    }
}