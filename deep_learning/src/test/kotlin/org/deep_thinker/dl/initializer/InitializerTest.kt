package org.deep_thinker.dl.initializer

import org.deep_thinker.dl.math.Matrix
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.sqrt

class InitializerTest {
    @Test
    fun testHeUniform() {
        val input = 700
        val m: Matrix = Matrix(300, input)
        HeUniform().initWeights(m, 0)
        assertEquals(0.0, m.average(), 0.001)
        assertEquals(varOnUniformDist(-sqrt(6.0 / input), sqrt(6.0 / input)), m.variance(), 0.001)
    }

    @Test
    fun testHeNormal() {
        val input = 700
        val m: Matrix = Matrix(300, input)
        HeNormal().initWeights(m, 0)
        assertEquals(0.0, m.average(), 0.001)
        assertEquals(2.0 / input, m.variance(), 0.001)
    }

    @Test
    fun testXavierUniform() {
        val input = 700
        val out = 300
        val m: Matrix = Matrix(out, input)
        XavierUniform().initWeights(m, 0)
        assertEquals(0.0, m.average(), 0.001)
        assertEquals(varOnUniformDist(-sqrt(6.0 / (input + out)), sqrt(6.0 / (input + out))), m.variance(), 0.001)
    }

    @Test
    fun testXavierNormal() {
        val input = 700
        val out = 300
        val m: Matrix = Matrix(out, input)
        XavierNormal().initWeights(m, 0)
        assertEquals(0.0, m.average(), 0.001)
        assertEquals(2.0 / (input + out), m.variance(), 0.001)
    }


    @Test
    fun testLecunUniform() {
        val input = 700
        val out = 300
        val m: Matrix = Matrix(out, input)
        LeCunUniform().initWeights(m, 0)
        assertEquals(0.0, m.average(), 0.001)
        assertEquals(varOnUniformDist(-sqrt(3.0 / input), sqrt(3.0 / input)), m.variance(), 0.0001)
    }

    @Test
    fun testLecunNormal() {
        val input = 700
        val out = 300
        val m: Matrix = Matrix(out, input)
        LeCunNormal().initWeights(m, 0)
        assertEquals(0.0, m.average(), 0.001)
        assertEquals(2.0 / (input + out), m.variance(), 0.001)
    }

    private fun varOnUniformDist(a: Double, b: Double): Double {
        return (b - a) * (b - a) / 12.0
    }
}