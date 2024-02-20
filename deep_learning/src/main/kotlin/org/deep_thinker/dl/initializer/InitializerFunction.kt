package org.deep_thinker.dl.initializer

import org.deep_thinker.dl.math.Matrix

class InitializerFunction(val delegate: (w: Matrix, l: Int) -> Unit) : Initializer {
    override fun initWeights(weights: Matrix, layer: Int) {
        delegate(weights, layer)
    }
}