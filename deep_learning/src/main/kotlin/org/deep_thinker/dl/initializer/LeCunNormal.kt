package org.deep_thinker.dl.initializer

import org.deep_thinker.dl.math.Matrix
import org.deep_thinker.dl.math.SharedRnd.getRnd
import kotlin.math.sqrt

class LeCunNormal : Initializer {
    override fun initWeights(weights: Matrix, layer: Int) {
        val factor = 1.0 / sqrt(weights.cols().toDouble())
        weights.map { value -> getRnd().nextGaussian() * factor }
    }
}