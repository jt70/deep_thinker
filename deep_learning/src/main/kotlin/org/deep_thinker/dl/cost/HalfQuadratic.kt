package org.deep_thinker.dl.cost

import org.deep_thinker.dl.math.Vec

class HalfQuadratic : CostFunction {
    override fun getName(): String {
        return "HalfQuadratic"
    }

    override fun getTotal(expected: Vec, actual: Vec): Double {
        val diff: Vec = expected.sub(actual)
        return diff.dot(diff) * 0.5
    }

    override fun getDerivative(expected: Vec, actual: Vec): Vec {
        return actual.sub(expected)
    }
}