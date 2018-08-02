import org.gradle.kotlin.dsl.`kotlin-dsl`

buildscript {
    val kotlinVersion = "1.2.60"

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

plugins {
    `kotlin-dsl`
}
