import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.gradle.api.plugins.ExtensionAware
import org.junit.platform.gradle.plugin.FiltersExtension
import org.junit.platform.gradle.plugin.EnginesExtension
import org.junit.platform.gradle.plugin.JUnitPlatformExtension

plugins {
    java
    id("org.jetbrains.kotlin.jvm")
    id("org.junit.platform.gradle.plugin")
    `maven-publish`
}

kotlin.experimental.coroutines = Coroutines.ENABLE

dependencies {
    implementation(Libraries.kotlinStdLib)
    implementation(Libraries.kotlinCoroutines)
    implementation(Libraries.kotlinLogging)
    testCompile(TestLibraries.kluent)
    testCompile(TestLibraries.spekAPI)
    testRuntime(TestLibraries.spekJUnitPlatformEngine)
}

publishing {
    publications {
        create("default", MavenPublication::class.java) {
            from(components["java"])
        }
    }
    repositories {
        maven(url = "$rootDir/build/repository")
    }
}

val compileKotlin by tasks.getting(KotlinCompile::class) {
    kotlinOptions.jvmTarget = "1.8"
}
val compileTestKotlin by tasks.getting(KotlinCompile::class) {
    kotlinOptions.jvmTarget = "1.8"
}

junitPlatform {
    filters {
        engines {
            include("spek")
        }
    }
}

// extension for configuration
fun JUnitPlatformExtension.filters(setup: FiltersExtension.() -> Unit) {
    when (this) {
        is ExtensionAware -> extensions.getByType(FiltersExtension::class.java).setup()
        else -> throw IllegalArgumentException("${this::class} must be an instance of ExtensionAware")
    }
}
fun FiltersExtension.engines(setup: EnginesExtension.() -> Unit) {
    when (this) {
        is ExtensionAware -> extensions.getByType(EnginesExtension::class.java).setup()
        else -> throw IllegalArgumentException("${this::class} must be an instance of ExtensionAware")
    }
}

//
//
//import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
//
//buildscript {
//    var kotlin_version: String by extra
//    kotlin_version = "1.2.41"
//
//    repositories {
//        mavenCentral()
//    }
//    dependencies {
//        classpath(kotlinModule("gradle-plugin", kotlin_version))
//    }
//}
//
//group = "com.malinskiy"
//version = "0.1.0"
//
//apply {
//    plugin("kotlin")
//}
//
//val kotlin_version: String by extra
//
//repositories {
//    mavenCentral()
//}
//
//dependencies {
//    compile(kotlinModule("stdlib-jdk8", kotlin_version))
//}
//
//tasks.withType<KotlinCompile> {
//    kotlinOptions.jvmTarget = "1.8"
//}