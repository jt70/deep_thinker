package org.deep_thinker.dl.activation

import org.deep_thinker.dl.math.Vec
import org.deep_thinker.dl.math.Function
import kotlin.math.exp
import kotlin.math.ln

@Suppress("unused")
open class Activation {
    val name: String
    private lateinit var fn: Function
    private lateinit var dFn: Function

    constructor(name: String) {
        this.name = name
    }

    constructor(name: String, fn: Function, dFn: Function) {
        this.name = name
        this.fn = fn
        this.dFn = dFn
    }

    // For most activation function it suffice to map each separate element.
    // I.e. they depend only on the single component in the vector.
    open fun fn(input: Vec): Vec {
        return input.map(fn)
    }

    fun dFn(out: Vec): Vec {
        return out.map(dFn)
    }

    // Also when calculating the Error change rate in terms of the input (dCdI)
    // it is just a matter of multiplying, i.e. ∂C/∂I = ∂C/∂O * ∂O/∂I.
    open fun dCdI(out: Vec, dCdO: Vec): Vec {
        return dCdO.elementProduct(dFn(out))
    }


    companion object {
        // --------------------------------------------------------------------------
        // --- A few predefined ones ------------------------------------------------
        // --------------------------------------------------------------------------
        // The simple properties of most activation functions as stated above makes
        // it easy to create the majority of them by just providing lambdas for
        // fn and the diff dfn.
        var ReLU: Activation = Activation(
            "ReLU",
            Function { x -> if (x <= 0) 0.0 else x },  // fn
            Function { x -> if (x <= 0) 0.0 else 1.0 } // dFn
        )

        var Leaky_ReLU: Activation = Activation(
            "Leaky_ReLU",
            Function { x -> if (x <= 0) 0.01 * x else x },  // fn
            Function { x -> if (x <= 0) 0.01 else 1.0 } // dFn
        )


        var Sigmoid: Activation = Activation(
            "Sigmoid",
            Function { x: Double -> sigmoidFn(x) },  // fn
            Function { x -> sigmoidFn(x) * (1.0 - sigmoidFn(x)) } // dFn
        )


        var Softplus: Activation = Activation(
            "Softplus",
            Function { x -> ln(1.0 + exp(x)) },  // fn
            Function { x: Double -> sigmoidFn(x) } // dFn
        )

        var Identity: Activation = Activation(
            "Identity",
            Function { x -> x },  // fn
            Function { x -> 1.0 } // dFn
        )

        // --------------------------------------------------------------------------
        private fun sigmoidFn(x: Double): Double {
            return 1.0 / (1.0 + exp(-x))
        }
    }
}

class Softmax : Activation("Softmax") {
    override fun fn(input: Vec): Vec {
        val data: DoubleArray = input.data
        var sum = 0.0
        val max: Double = input.max() // Trick: translate the input by largest element to avoid overflow.
        for (a in data) sum += exp(a - max)

        val finalSum = sum
        return input.map { a -> exp(a - max) / finalSum }
    }

    override fun dCdI(out: Vec, dCdO: Vec): Vec {
        val x: Double = out.elementProduct(dCdO).sumElements()
        val sub: Vec = dCdO.sub(x)
        return out.elementProduct(sub)
    }
}

