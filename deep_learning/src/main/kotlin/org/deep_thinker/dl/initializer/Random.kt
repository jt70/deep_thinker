package org.deep_thinker.dl.initializer

import org.deep_thinker.dl.math.Matrix
import org.deep_thinker.dl.math.SharedRnd.getRnd

class Random(val min: Double, val max: Double) : Initializer {
    override fun initWeights(weights: Matrix, layer: Int) {
        val delta: Double = max - min
        weights.map { value -> min + getRnd().nextDouble() * delta }
    }
}
