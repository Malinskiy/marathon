object Versions {
    val marathon = System.getenv("GIT_TAG_NAME") ?: "0.7.0"

    val kotlin = "1.4.31"
    val coroutines = "1.4.3"

    val ddmlib = "27.1.2"
    val adam = "0.3.2"
    val dexTestParser = "2.3.3"
    val kotlinLogging = "2.0.8"
    val logbackClassic = "1.2.3"
    val axmlParser = "1.0"
    val bugsnag = "3.6.2"

    val junitGradle = "1.2.0"
    val androidGradleVersion = "4.0.0"

    val junit5 = "5.7.2"
    val kluent = "1.65"

    val kakao = "3.0.2"
    val espresso = "3.0.1"
    val espressoRules = "1.0.1"
    val espressoRunner = "1.0.1"
    val junit = "4.12"
    val gson = "2.8.7"
    val apacheCommonsText = "1.9"
    val apacheCommonsIO = "2.9.0"
    val influxDbClient = "2.21"
    val argParser = "2.0.7"
    val jacksonDatabind = "2.12.3"
    val jacksonKotlin = jacksonDatabind
    val jacksonYaml = jacksonDatabind
    val jacksonJSR310 = jacksonDatabind
    val ddPlist = "1.23"
    val guava = "30.1.1-jre"
    val rsync4j = "3.2.3-3"
    val sshj = "0.31.0"
    val testContainers = "1.15.3"
    val jupiterEngine = junit5
    val jansi = "2.3.2"
    val scalr = "4.2"
    val allureJava = "2.14.0"
    val allureKotlin = "2.2.6"
    val allureEnvironment = "1.0.0"
    val mockitoKotlin = "2.2.0"
    val googleAnalitycsWrapper = "2.0.0"
    val dokka = "1.4.32"
    val koin = "3.0.2"
    val jsonAssert = "1.5.0"
    val xmlUnit = "2.8.2"
    val assertk = "0.19"
}

object BuildPlugins {
    val kotlinPlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    val junitGradle = "org.junit.platform:junit-platform-gradle-plugin:${Versions.junitGradle}"
    val androidGradle = "com.android.tools.build:gradle:${Versions.androidGradleVersion}"
    val dokka = "org.jetbrains.dokka:dokka-gradle-plugin:${Versions.dokka}"
}

object Libraries {
    val ddmlib = "com.android.tools.ddms:ddmlib:${Versions.ddmlib}"
    val adam = "com.malinskiy.adam:adam:${Versions.adam}"
    val androidCommon = "com.android.tools:common:${Versions.ddmlib}"
    val dexTestParser = "com.linkedin.dextestparser:parser:${Versions.dexTestParser}"
    val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    val kotlinStdLibCommon = "org.jetbrains.kotlin:kotlin-stdlib-common:${Versions.kotlin}"
    val kotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}"
    val kotlinLogging = "io.github.microutils:kotlin-logging:${Versions.kotlinLogging}"
    val logbackClassic = "ch.qos.logback:logback-classic:${Versions.logbackClassic}"
    val kotlinCoroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    val axmlParser = "com.shazam:axmlparser:${Versions.axmlParser}"
    val gson = "com.google.code.gson:gson:${Versions.gson}"
    val apacheCommonsText = "org.apache.commons:commons-text:${Versions.apacheCommonsText}"
    val apacheCommonsIO = "commons-io:commons-io:${Versions.apacheCommonsIO}"
    val influxDbClient = "org.influxdb:influxdb-java:${Versions.influxDbClient}"
    val argParser = "com.xenomachina:kotlin-argparser:${Versions.argParser}"
    val jacksonDatabind = "com.fasterxml.jackson.core:jackson-databind:${Versions.jacksonDatabind}"
    val jacksonAnnotations = "com.fasterxml.jackson.core:jackson-annotations:${Versions.jacksonDatabind}"
    val jacksonKotlin = "com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jacksonKotlin}"
    val jacksonYaml = "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${Versions.jacksonYaml}"
    val jacksonJSR310 = "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${Versions.jacksonJSR310}"
    val ddPlist = "com.googlecode.plist:dd-plist:${Versions.ddPlist}"
    val guava = "com.google.guava:guava:${Versions.guava}"
    val rsync4j = "com.github.fracpete:rsync4j-all:${Versions.rsync4j}"
    val sshj = "com.hierynomus:sshj:${Versions.sshj}"
    val jansi = "org.fusesource.jansi:jansi:${Versions.jansi}"
    val scalr = "org.imgscalr:imgscalr-lib:${Versions.scalr}"
    val allure = "io.qameta.allure:allure-java-commons:${Versions.allureJava}"
    val allureEnvironment = "com.github.automatedowl:allure-environment-writer:${Versions.allureEnvironment}"
    val allureKotlinCommons = "io.qameta.allure:allure-kotlin-commons:${Versions.allureKotlin}"
    val koin = "io.insert-koin:koin-core:${Versions.koin}"
    val bugsnag = "com.bugsnag:bugsnag:${Versions.bugsnag}"
}

object TestLibraries {
    val junit5 = "org.junit.jupiter:junit-jupiter:${Versions.junit5}"
    val kluent = "org.amshove.kluent:kluent:${Versions.kluent}"
    val kakao = "io.github.kakaocup:kakao:${Versions.kakao}"

    val junit = "junit:junit:${Versions.junit}"
    val mockitoKotlin = "com.nhaarman.mockitokotlin2:mockito-kotlin:${Versions.mockitoKotlin}"
    val jupiterEngine = "org.junit.jupiter:junit-jupiter-engine:${Versions.jupiterEngine}"
    val koin = "io.insert-koin:koin-test:${Versions.koin}"
    val jsonAssert = "org.skyscreamer:jsonassert:${Versions.jsonAssert}"
    val xmlUnit = "org.xmlunit:xmlunit-matchers:${Versions.xmlUnit}"
    val assertk = "com.willowtreeapps.assertk:assertk:${Versions.assertk}"

    val testContainers = "org.testcontainers:testcontainers:${Versions.testContainers}"
    val testContainersInflux = "org.testcontainers:influxdb:${Versions.testContainers}"
    val adamServerStubJunit5 = "com.malinskiy.adam:server-stub-junit5:${Versions.adam}"
}

object Analytics {
    val googleAnalyticsWrapper = "com.brsanthu:google-analytics-java:${Versions.googleAnalitycsWrapper}"
}
