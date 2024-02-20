package org.deep_thinker.dl.initializer

import org.deep_thinker.dl.math.Matrix
import org.deep_thinker.dl.math.SharedRnd.getRnd
import kotlin.math.sqrt

class HeNormal : Initializer {
    override fun initWeights(weights: Matrix, layer: Int) {
        val factor = sqrt(2.0 / weights.cols())
        weights.map { value -> getRnd().nextGaussian() * factor }
    }
}