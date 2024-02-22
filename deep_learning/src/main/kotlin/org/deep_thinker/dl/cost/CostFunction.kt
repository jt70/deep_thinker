package org.deep_thinker.dl.cost

import org.deep_thinker.dl.math.Vec

interface CostFunction {

    fun getName(): String

    fun getTotal(expected: Vec, actual: Vec): Double

    fun getDerivative(expected: Vec, actual: Vec): Vec
}
