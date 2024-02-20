package org.deep_thinker.dl.cost

import org.deep_thinker.dl.math.Vec
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CostFunctionTest {
    @Test
    fun testQuadraticCost() {
        val expected: Vec = Vec(1, 2, 3)
        val actual: Vec = Vec(4, -3, 7)
        val cost = Quadratic().getTotal(expected, actual)
        assertEquals((3 * 3 + 5 * 5 + 4 * 4).toDouble(), cost, 0.01)

        val err: Vec = Quadratic().getDerivative(expected, actual)
        assertEquals(Vec(3, -5, 4).mul(2.0), err)
    }

    @Test
    fun testQuadraticCost3() {
        val expected: Vec = Vec(doubleArrayOf(1.0, 0.2))
        val actual: Vec = Vec(doubleArrayOf(0.712257432295742, 0.533097573871501))
        val cost = Quadratic().getTotal(expected, actual)
        assertEquals(0.19374977898811957, cost, 0.0000001)
    }
}