package org.deep_thinker.dl.cost

import org.deep_thinker.dl.math.Vec

class Quadratic : CostFunction {
    override fun getName(): String {
        return "Quadratic"
    }

    override fun getTotal(expected: Vec, actual: Vec): Double {
        val diff: Vec = actual.sub(expected)
        return diff.dot(diff)
    }

    override fun getDerivative(expected: Vec, actual: Vec): Vec {
        return actual.sub(expected).mul(2.0)
    }
}
