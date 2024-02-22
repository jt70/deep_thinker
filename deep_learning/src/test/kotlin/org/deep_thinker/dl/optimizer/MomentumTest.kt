package org.deep_thinker.dl.optimizer

import org.deep_thinker.dl.math.Matrix
import org.deep_thinker.dl.math.Vec
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test

class MomentumTest {
    @Test
    fun testMomentumWeightUpdate() {
        val W: Matrix = Matrix(arrayOf<DoubleArray>(doubleArrayOf(2.0, 3.0, 4.0), doubleArrayOf(3.0, 4.0, 5.0)))
        val dW: Matrix = Matrix(arrayOf<DoubleArray>(doubleArrayOf(.2, .3, .4), doubleArrayOf(.3, .4, .5)))
        val o: Optimizer = Momentum(0.05)
        o.updateWeights(W, dW)
        assertArrayEquals(doubleArrayOf(1.990, 2.985, 3.980), W.data.get(0), EPS)
        assertArrayEquals(doubleArrayOf(2.985, 3.980, 4.975), W.data.get(1), EPS)
        o.updateWeights(W, dW)
        assertArrayEquals(doubleArrayOf(1.9710, 2.9565, 3.9420), W.data.get(0), EPS)
        assertArrayEquals(doubleArrayOf(2.9565, 3.9420, 4.9275), W.data.get(1), EPS)
    }

    @Test
    fun testMomentumBiasUpdate() {
        var bias: Vec = Vec(2, 3, 4)
        val db: Vec = Vec(doubleArrayOf(.2, .3, .4))
        val o: Optimizer = Momentum(0.05)
        bias = o.updateBias(bias, db)
        assertArrayEquals(doubleArrayOf(1.990, 2.985, 3.980), bias.data, EPS)
        bias = o.updateBias(bias, db)
        assertArrayEquals(doubleArrayOf(1.9710, 2.9565, 3.9420), bias.data, EPS)
    }

    companion object {
        const val EPS: Double = 0.0000001
    }
}
