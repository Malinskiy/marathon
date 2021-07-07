package com.malinskiy.marathon.test

import com.malinskiy.marathon.Marathon
import com.malinskiy.marathon.test.factory.MarathonFactory
import kotlinx.coroutines.runBlocking

fun setupMarathon(f: suspend MarathonFactory.() -> Unit): Marathon {
    return runBlocking {
        val marathonFactory = MarathonFactory()
        f(marathonFactory)
        marathonFactory.build()
    }
}
