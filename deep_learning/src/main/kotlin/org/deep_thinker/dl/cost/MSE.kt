package org.deep_thinker.dl.cost

import org.deep_thinker.dl.math.Vec

class MSE : CostFunction {
    override fun getName(): String {
        return "MSE"
    }

    override fun getTotal(expected: Vec, actual: Vec): Double {
        val diff: Vec = expected.sub(actual)
        return diff.dot(diff) / actual.dimension()
    }

    override fun getDerivative(expected: Vec, actual: Vec): Vec {
        return actual.sub(expected).mul(2.0 / actual.dimension())
    }
}
