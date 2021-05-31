buildscript {
    repositories {
        jcenter()
        mavenCentral()
        google()
    }
    dependencies {
        classpath(BuildPlugins.kotlinPlugin)
        classpath(BuildPlugins.androidGradle)
        /**
         * Starting with kotlin plugin 1.3.41 coroutines dependency is not propagated to the classpath of gradle plugin
         *
         * e.g.
         * Caused by: java.lang.NoSuchMethodError: kotlinx.coroutines.channels.ChannelIterator.next()Ljava/lang/Object;
         *
         * Hence we need to explicitly add coroutines to our classpath
         */
        classpath(Libraries.kotlinCoroutines)
    }
}

allprojects {
    repositories {
        mavenLocal()
        maven { url = uri("$rootDir/../build/repository") }
        jcenter()
        mavenCentral()
        google()
        mavenLocal()
    }
}
