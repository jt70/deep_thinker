package org.deep_thinker.dl.optimizer

import org.deep_thinker.dl.math.Matrix
import org.deep_thinker.dl.math.Vec

class GradientDescent(val learningRate: Double) : Optimizer {

    override fun updateWeights(weights: Matrix, dCdW: Matrix) {
        weights.sub(dCdW.mul(learningRate))
    }

    override fun updateBias(bias: Vec, dCdB: Vec): Vec {
        return bias.sub(dCdB.mul(learningRate))
    }

    override fun copy(): Optimizer {
        // no need to make copies since this optimizer has
        // no state. Same instance can serve all layers.
        return this
    }
}