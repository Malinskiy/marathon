import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
}

dependencies {
    implementation(Libraries.kotlinStdLib)
    implementation(Libraries.kotlinCoroutines)
    implementation(Libraries.kotlinLogging)
    implementation(Libraries.androidCommon)
    implementation(Libraries.jacksonAnnotations)
    implementation(project(":core"))
    implementation(project(":vendor:vendor-android:base"))
    implementation(Libraries.logbackClassic)
    api(Libraries.adam)
    api(Libraries.adamTestrunnerContract)
    testImplementation(project(":vendor:vendor-test"))
    testImplementation(TestLibraries.kluent)
    testImplementation(TestLibraries.mockitoKotlin)
    testImplementation(TestLibraries.koin)
    testImplementation(TestLibraries.junit5)
    testImplementation(TestLibraries.jupiterEngine)
}

Deployment.initialize(project)

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.apiVersion = "1.4"
}
