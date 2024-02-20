package org.deep_thinker.dl.initializer

import org.deep_thinker.dl.math.Matrix
import org.deep_thinker.dl.math.SharedRnd.getRnd
import kotlin.math.sqrt

class LeCunUniform : Initializer {
    override fun initWeights(weights: Matrix, layer: Int) {
        val factor = 2.0 * sqrt(3.0 / weights.cols())
        weights.map { value -> (getRnd().nextDouble() - 0.5) * factor }
    }
}