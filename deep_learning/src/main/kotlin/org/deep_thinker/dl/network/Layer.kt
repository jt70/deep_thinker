package org.deep_thinker.dl.network

import org.deep_thinker.dl.activation.Activation
import org.deep_thinker.dl.math.Matrix
import org.deep_thinker.dl.math.Vec
import org.deep_thinker.dl.optimizer.Optimizer

class Layer {
    private val size: Int
    private val out: ThreadLocal<Vec> = ThreadLocal<Vec>()
    private val activation: Activation
    lateinit private var optimizer: Optimizer
    private var weights: Matrix? = null
    private var bias: Vec
    private var l2 = 0.0

    var precedingLayer: Layer? = null

    // Not yet realized changes to the weights and biases ("observed things not yet learned")
    @Transient
    private lateinit var deltaWeights: Matrix

    @Transient
    private var deltaBias: Vec

    @Transient
    private var deltaWeightsAdded = 0

    @Transient
    private var deltaBiasAdded = 0

    constructor(size: Int, activation: Activation) : this(size, activation, 0.0)

    constructor(size: Int, activation: Activation, initialBias: Double) {
        this.size = size
        bias = Vec(size).map { x -> initialBias }
        deltaBias = Vec(size)
        this.activation = activation
    }

    constructor(size: Int, activation: Activation, bias: Vec) {
        this.size = size
        this.bias = bias
        deltaBias = Vec(size)
        this.activation = activation
    }

    fun size(): Int {
        return size
    }

    /**
     * Feed the in-vector, i, through this layer.
     * Stores a copy of the out vector.
     *
     * @param i The input vector
     * @return The out vector o (i.e. the result of o = iW + b)
     */
    fun evaluate(i: Vec): Vec {
        if (!hasPrecedingLayer()) {
            out.set(i) // No calculation i input layer, just store data
        } else {
            out.set(activation.fn(i.mul(weights!!).add(bias)))
        }
        return out.get()
    }

    fun getOut(): Vec {
        return out.get()
    }

    fun getActivation(): Activation {
        return activation
    }

    fun setWeights(weights: Matrix) {
        this.weights = weights
        deltaWeights = Matrix(weights.rows(), weights.cols())
    }

    fun setOptimizer(optimizer: Optimizer) {
        this.optimizer = optimizer
    }

    fun setL2(l2: Double) {
        this.l2 = l2
    }

    fun hasPrecedingLayer(): Boolean {
        return precedingLayer != null
    }

    fun getBias(): Vec {
        return bias
    }

    /**
     * Add upcoming changes to the Weights and Biases.
     * This does not mean that the network is updated.
     */
    @Synchronized
    fun addDeltaWeightsAndBiases(dW: Matrix, dB: Vec) {
        deltaWeights.add(dW)
        deltaWeightsAdded++
        deltaBias = deltaBias.add(dB)
        deltaBiasAdded++
    }

    /**
     * Takes an average of all added Weights and Biases and tell the
     * optimizer to apply them to the current weights and biases.
     *
     *
     * Also applies L2 regularization on the weights if used.
     */
    @Synchronized
    fun updateWeightsAndBias() {
        if (deltaWeightsAdded > 0) {
            if (l2 > 0) weights!!.map { value -> value - l2 * value }

            val average_dW: Matrix = deltaWeights.mul(1.0 / deltaWeightsAdded)
            optimizer.updateWeights(weights!!, average_dW)
            deltaWeights.map { a -> 0.0 } // Clear
            deltaWeightsAdded = 0
        }

        if (deltaBiasAdded > 0) {
            val average_bias: Vec = deltaBias.mul(1.0 / deltaBiasAdded)
            bias = optimizer!!.updateBias(bias, average_bias)
            deltaBias = deltaBias.map { a -> 0.0 } // Clear
            deltaBiasAdded = 0
        }
    }

    fun getWeights(): Matrix? {
        return weights
    }

    val state: LayerState
        // ------------------------------------------------------------------
        get() = LayerState(this)

    @Suppress("unused")
    class LayerState(layer: Layer) {
        var weights: Array<DoubleArray>? = if (layer.weights != null) layer.weights!!.data else null
        var bias: DoubleArray = layer.getBias().data
        var activation: String = layer.activation.name
    }
}
