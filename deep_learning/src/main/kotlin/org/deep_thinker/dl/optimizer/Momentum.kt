package org.deep_thinker.dl.optimizer

import org.deep_thinker.dl.math.Matrix
import org.deep_thinker.dl.math.Vec

class Momentum(val learningRate: Double, val momentum: Double = 0.9) : Optimizer {
    private var lastDW: Matrix? = null
    private var lastDBias: Vec? = null

    override fun updateWeights(weights: Matrix, dCdW: Matrix) {
        if (lastDW == null) {
            lastDW = dCdW.copy().mul(learningRate)
        } else {
            lastDW!!.mul(momentum).add(dCdW.copy().mul(learningRate))
        }
        weights.sub(lastDW!!)
    }

    override fun updateBias(bias: Vec, dCdB: Vec): Vec {
        lastDBias = if (lastDBias == null) {
            dCdB.mul(learningRate)
        } else {
            lastDBias!!.mul(momentum).add(dCdB.mul(learningRate))
        }
        return bias.sub(lastDBias!!)
    }

    override fun copy(): Optimizer {
        return Momentum(learningRate, momentum)
    }
}