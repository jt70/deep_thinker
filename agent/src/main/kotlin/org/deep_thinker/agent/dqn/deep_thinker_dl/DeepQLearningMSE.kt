package org.deep_thinker.agent.dqn.deep_thinker_dl

import org.deep_thinker.dl.cost.CostFunction
import org.deep_thinker.dl.math.Vec

class DeepQLearningMSE : CostFunction {
    override fun getName(): String {
        return "DeepQLearningMSE"
    }

    override fun getTotal(expected: Vec, actual: Vec): Double {
        val diff: Vec = expected.subZeroNan(actual)
        return diff.dot(diff) / actual.dimension()
    }

    override fun getDerivative(expected: Vec, actual: Vec): Vec {
        return actual.subZeroNan(expected).mul(2.0 / actual.dimension())
    }
}
