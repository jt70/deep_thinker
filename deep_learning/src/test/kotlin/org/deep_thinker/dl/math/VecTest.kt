package org.deep_thinker.dl.math

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class VecTest {
    @Test
    fun dot() {
        val v = Vec(1, 2, 3)
        val u = Vec(3, -4, 5)
        assertEquals(v.dot(u), 10.0, EPS)
        assertEquals(u.dot(v), 10.0, EPS)
    }


    @Test
    fun test_cMultiply() {
        val a = Vec(1, 2, 3)
        val b = Vec(1, 2, 3, 4, 5, 6)

        val result = a.outerProduct(b)
        assertEquals(result.rows(), 6)
        assertEquals(result.cols(), 3)

        assertEquals(result.data.get(0).get(0), 1.0, EPS)
        assertEquals(result.data.get(0).get(2), 3.0, EPS)
        assertEquals(result.data.get(2).get(0), 3.0, EPS)
        assertEquals(result.data.get(5).get(0), 6.0, EPS)
        assertEquals(result.data.get(5).get(2), 18.0, EPS)
    }

    @Test
    fun test_multiply() {
        val v = Vec(1, 2) // 1x2
        val m = Matrix(arrayOf(doubleArrayOf(2.0, 1.0, 3.0), doubleArrayOf(3.0, 4.0, -1.0))) // 2x3
        val res = v.mul(m)

        assertEquals(res.dimension(), 3)
        assertEquals(res.data.get(0), 8.0, EPS)
        assertEquals(res.data.get(1), 9.0, EPS)
        assertEquals(res.data.get(2), 1.0, EPS)
    }


    @Test
    fun sub() {
        assertEquals(Vec(-1, -4, 1), Vec(1, -2, 3).sub(Vec(2, 2, 2)))
    }

    @Test
    fun add() {
        assertEquals(Vec(3, -4, 1), Vec(1, -2, 3).add(Vec(2, -2, -2)))
    }

    @Test
    fun index() {
        assertEquals(3, Vec(1, -2, 3, 5, -25).indexOfLargestElement())
    }

    companion object {
        const val EPS: Double = 0.000001
    }
}
