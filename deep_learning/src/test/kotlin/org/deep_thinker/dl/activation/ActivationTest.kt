package org.deep_thinker.dl.activation

import org.deep_thinker.dl.activation.Activation
import org.deep_thinker.dl.activation.Softmax
import org.deep_thinker.dl.math.Vec
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class ActivationTest {
    @Test
    fun testSoftMax() {
        val softmax: Activation = Softmax()
        val v: Vec = softmax.fn(Vec(doubleArrayOf(-1.0, 0.0, 1.5, 2.0)))
        assertEquals(v, Vec(doubleArrayOf(0.02778834297343303, 0.0755365477476706, 0.3385313204518047, 0.5581437888270917)))
    }

    @Test
    fun testLReLU() {
        val softmax = Activation.Leaky_ReLU
        val v: Vec = softmax.fn(Vec(doubleArrayOf(-1.0, 0.0, 1.5, 2.0)))
        assertEquals(v, Vec(doubleArrayOf(-0.01, 0.0, 1.5, 2.0)))
    }
}