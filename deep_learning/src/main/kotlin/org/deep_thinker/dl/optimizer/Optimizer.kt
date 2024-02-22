package org.deep_thinker.dl.optimizer

import org.deep_thinker.dl.math.Matrix
import org.deep_thinker.dl.math.Vec

interface Optimizer {
    fun updateWeights(weights: Matrix, dCdW: Matrix)

    fun updateBias(bias: Vec, dCdB: Vec): Vec

    fun copy(): Optimizer
}