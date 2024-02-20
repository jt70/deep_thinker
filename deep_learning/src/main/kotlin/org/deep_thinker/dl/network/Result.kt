package org.deep_thinker.dl.network

import org.deep_thinker.dl.math.Vec

class Result {
    private val output: Vec
    val cost: Double?

    constructor(output: Vec) {
        this.output = output
        cost = null
    }

    constructor(output: Vec, cost: Double) {
        this.output = output
        this.cost = cost
    }

    fun getOutput(): Vec {
        return output
    }

    override fun toString(): String {
        return "Result{" + "output=" + output +
                ", cost=" + cost +
                '}'
    }
}
