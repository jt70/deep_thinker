package org.deep_thinker.dl.initializer

import org.deep_thinker.dl.math.Matrix

interface Initializer {
    fun initWeights(weights: Matrix, layer: Int)
}