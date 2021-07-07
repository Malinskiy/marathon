package com.example

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

import java.util.Arrays

import org.junit.Assert.assertEquals

@RunWith(Parameterized::class)
class NamedParameterizedTest(private val input: Int, private val expected: Int) {

    @Test
    fun test() {
        assertEquals(expected.toLong(), compute(input).toLong())
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "compute test for input {0}: {1}")
        fun data(): Collection<Array<Any>> {
            return Arrays.asList(
                arrayOf<Any>(0, 0),
                arrayOf<Any>(1, 1),
                arrayOf<Any>(2, 1),
                arrayOf<Any>(3, 2),
                arrayOf<Any>(4, 3),
                arrayOf<Any>(5, 5),
                arrayOf<Any>(6, 8)
            )
        }

        fun compute(n: Int): Int {
            return if (n <= 1) {
                n
            } else {
                compute(n - 1) + compute(n - 2)
            }
        }
    }
}
