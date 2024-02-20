package org.deep_thinker.dl.math

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MatrixTest {
    /*
       [2 3 4]   [1]    [20]
       [3 4 5] * [2] =  [26]
                 [3]
        */
    @Test
    fun testMultiply() {
        val v = Vec(1, 2, 3)
        val W = Matrix(arrayOf(doubleArrayOf(2.0, 3.0, 4.0), doubleArrayOf(3.0, 4.0, 5.0)))
        val result = W.multiply(v)

        assertArrayEquals(doubleArrayOf(20.0, 26.0), result.data, 0.1)
    }

    @Test
    fun test_multiply() {
        val v = Vec(1, 2) // 1x2
        val m = Matrix(arrayOf(doubleArrayOf(2.0, 1.0, 3.0), doubleArrayOf(3.0, 4.0, -1.0))) // 2x3
        val res = v.mul(m)

        assertEquals(res.dimension(), 3)
        assertEquals(res.data.get(0), 8.0, 0.001)
        assertEquals(res.data.get(1), 9.0, 0.001)
        assertEquals(res.data.get(2), 1.0, 0.001)
    }

    @Test
    fun testMap() {
        var W = Matrix(arrayOf(doubleArrayOf(2.0, 3.0, 4.0), doubleArrayOf(3.0, 4.0, 5.0)))
        W = W.map { value -> 1.0 }

        assertEquals(1.0, W.data.get(0).get(0), 0.1)
        assertEquals(1.0, W.data.get(1).get(1), 0.1)
    }

    @Test
    fun testScale() {
        var W = Matrix(arrayOf(doubleArrayOf(2.0, 3.0, 4.0), doubleArrayOf(3.0, 4.0, 5.0)))
        W = W.mul(2.0)

        assertEquals(6.0, W.data.get(0).get(1), 0.1)
        assertEquals(8.0, W.data.get(1).get(1), 0.1)
    }

    @Test
    fun testAverage() {
        val U = Matrix(
            arrayOf(
                doubleArrayOf(2.0, 3.0),
                doubleArrayOf(3.0, 4.0)
            )
        )
        val V = Matrix(
            arrayOf(
                doubleArrayOf(4.0, 5.0),
                doubleArrayOf(6.0, 7.0)
            )
        )

        assertEquals(3.0, U.average(), 0.1)
        assertEquals(5.5, V.average(), 0.1)
    }

    @Test
    fun testVariance() {
        val U = Matrix(
            arrayOf(
                doubleArrayOf(11.0, 3.0),
                doubleArrayOf(3.0, 7.0)
            )
        )
        assertEquals(
            ((11 - 6) * (11 - 6) + ((6 - 3) * (6 - 3)) + ((6 - 3) * (6 - 3)) + ((7 - 6) * (7 - 6))) / 4.0,
            U.variance(),
            0.1
        )
    }
}