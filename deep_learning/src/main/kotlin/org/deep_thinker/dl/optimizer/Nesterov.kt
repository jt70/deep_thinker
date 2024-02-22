package org.deep_thinker.dl.optimizer

import org.deep_thinker.dl.math.Matrix
import org.deep_thinker.dl.math.Vec

/**
 * Updates Weights and biases based on:
 * v_prev = v
 * v = γ * v - η * dC/dW
 * W += -γ * v_prev + (1 + γ) * v
 *
 *
 * γ is the momentum (i.e. how much of the last gradient will we use again)
 * η is the learning rate
 *
 *
 */
@Suppress("unused")
class Nesterov (private val learningRate: Double, private val momentum: Double = 0.9) : Optimizer {
    private var lastDW: Matrix? = null
    private var lastDBias: Vec? = null

    override fun updateWeights(weights: Matrix, dCdW: Matrix) {
        if (lastDW == null) {
            lastDW = Matrix(dCdW.rows(), dCdW.cols())
        }
        val lastDWCopy: Matrix = lastDW!!.copy()
        lastDW!!.mul(momentum).sub(dCdW.mul(learningRate))
        weights.add(lastDWCopy.mul(-momentum).add(lastDW!!.copy().mul(1 + momentum)))
    }

    override fun updateBias(bias: Vec, dCdB: Vec): Vec {
        if (lastDBias == null) {
            lastDBias = Vec(dCdB.dimension())
        }
        val lastDBiasCopy: Vec? = lastDBias
        lastDBias = lastDBias!!.mul(momentum).sub(dCdB.mul(learningRate))
        return bias.add(lastDBiasCopy!!.mul(-momentum).add(lastDBias!!.mul(1 + momentum)))
    }

    override fun copy(): Optimizer {
        return Nesterov(learningRate, momentum)
    }
}
