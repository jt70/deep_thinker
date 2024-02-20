package org.deep_thinker.dl.initializer

import org.deep_thinker.dl.math.Matrix
import org.deep_thinker.dl.math.SharedRnd.getRnd
import kotlin.math.sqrt

class XavierUniform : Initializer {
    override fun initWeights(weights: Matrix, layer: Int) {
        val factor = 2.0 * sqrt(6.0 / (weights.cols() + weights.rows()))
        weights.map { value -> (getRnd().nextDouble() - 0.5) * factor }
    }
}