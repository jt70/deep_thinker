package org.deep_thinker.dl.network

import org.deep_thinker.dl.activation.Activation
import org.deep_thinker.dl.optimizer.Optimizer
import org.deep_thinker.dl.cost.CostFunction
import org.deep_thinker.dl.cost.Quadratic
import org.deep_thinker.dl.initializer.Initializer
import org.deep_thinker.dl.initializer.InitializerFunction
import org.deep_thinker.dl.initializer.Random
import org.deep_thinker.dl.math.Matrix
import org.deep_thinker.dl.math.Vec
import org.deep_thinker.dl.optimizer.GradientDescent

class NeuralNetwork(nb: Builder) {
    private val costFunction: CostFunction
    private val networkInputSize: Int
    private val l2: Double
    private val optimizer: Optimizer

    private val layers: MutableList<Layer> = ArrayList()

    /**
     * Creates a neural network given the configuration set in the builder
     *
     * @param nb The config for the neural network
     */
    init {
        costFunction = nb.costFunction
        networkInputSize = nb.networkInputSize
        optimizer = nb.optimizer
        l2 = nb.l2

        // Adding inputLayer
        val inputLayer = Layer(networkInputSize, Activation.Identity)
        layers.add(inputLayer)

        var precedingLayer = inputLayer

        for (i in nb.layers.indices) {
            val layer = nb.layers[i]
            val w: Matrix = Matrix(precedingLayer.size(), layer.size())
            nb.initializer.initWeights(w, i)
            layer.setWeights(w) // Each layer contains the weights between preceding layer and itself
            layer.setOptimizer(optimizer.copy())
            layer.setL2(l2)
            layer.precedingLayer = precedingLayer
            layers.add(layer)

            precedingLayer = layer
        }
    }


    /**
     * Evaluates an input vector, returning the networks output,
     * without cost or learning anything from it.
     */
    fun evaluate(input: Vec): Result {
        return evaluate(input, null)
    }


    /**
     * Evaluates an input vector, returning the networks output.
     * If `expected` is specified the result will contain
     * a cost and the network will gather some learning from this
     * operation.
     */
    fun evaluate(input: Vec, expected: Vec?): Result {
        var signal: Vec = input
        for (layer in layers) signal = layer.evaluate(signal)

        if (expected != null) {
            learnFrom(expected)
            val cost: Double = costFunction.getTotal(expected, signal)
            return Result(signal, cost)
        }

        return Result(signal)
    }


    /**
     * Will gather some learning based on the `expected` vector
     * and how that differs to the actual output from the network. This
     * difference (or error) is backpropagated through the net. To make
     * it possible to use mini batches the learning is not immediately
     * realized - i.e. `learnFrom` does not alter any weights.
     * Use `updateFromLearning()` to do that.
     */
    private fun learnFrom(expected: Vec) {
        var layer = lastLayer

        // The error is initially the derivative of the cost-function.
        var dCdO: Vec = costFunction.getDerivative(expected, layer.getOut())

        // iterate backwards through the layers
        do {
            val dCdI: Vec = layer.getActivation().dCdI(layer.getOut(), dCdO)
            val dCdW: Matrix = dCdI.outerProduct(layer.precedingLayer!!.getOut())

            // Store the deltas for weights and biases
            layer.addDeltaWeightsAndBiases(dCdW, dCdI)

            // prepare error propagation and store for next iteration
            dCdO = layer.getWeights()!!.multiply(dCdI)

            layer = layer.precedingLayer!!
        } while (layer.hasPrecedingLayer()) // Stop when we are at input layer
    }


    /**
     * Let all gathered (but not yet realised) learning "sink in".
     * That is: Update the weights and biases based on the deltas
     * collected during evaluation & training.
     */
    @Synchronized
    fun updateFromLearning() {
        for (l in layers) if (l.hasPrecedingLayer()) // Skip input layer
            l.updateWeightsAndBias()
    }


    // --------------------------------------------------------------------
    fun getLayers(): List<Layer> {
        return layers
    }

    fun toJson(pretty: Boolean): String {
        val gsonBuilder: com.google.gson.GsonBuilder = com.google.gson.GsonBuilder()
        if (pretty) gsonBuilder.setPrettyPrinting()
        return gsonBuilder.create().toJson(NetworkState(this))
    }


    private val lastLayer: Layer
        get() = layers[layers.size - 1]


    // --------------------------------------------------------------------
    /**
     * Simple builder for a NeuralNetwork
     */
    class Builder {
        val layers: MutableList<Layer> = ArrayList()
        val networkInputSize: Int

        // defaults:
        var initializer: Initializer = Random(-0.5, 0.5)
        internal var costFunction: CostFunction = Quadratic()
        var optimizer: Optimizer = GradientDescent(0.005)
        var l2: Double = 0.0

        constructor(networkInputSize: Int) {
            this.networkInputSize = networkInputSize
        }

        /**
         * Create a builder from an existing neural network, hence making
         * it possible to do a copy of the entire state and modify as needed.
         */
        constructor(other: NeuralNetwork) {
            networkInputSize = other.networkInputSize
            costFunction = other.costFunction
            optimizer = other.optimizer
            l2 = other.l2

            val otherLayers = other.getLayers()
            for (i in 1 until otherLayers.size) {
                val otherLayer = otherLayers[i]
                layers.add(
                    Layer(
                        otherLayer.size(),
                        otherLayer.getActivation(),
                        otherLayer.getBias()
                    )
                )
            }

            initializer = InitializerFunction { weights, layer ->
                val otherLayer = otherLayers[layer + 1]
                val otherLayerWeights: Matrix = otherLayer.getWeights()!!
                weights.fillFrom(otherLayerWeights)
            }
        }

        fun initWeights(initializer: Initializer): Builder {
            this.initializer = initializer
            return this
        }

        fun setCostFunction(costFunction: CostFunction): Builder {
            this.costFunction = costFunction
            return this
        }

        fun setOptimizer(optimizer: Optimizer): Builder {
            this.optimizer = optimizer
            return this
        }

        fun l2(l2: Double): Builder {
            this.l2 = l2
            return this
        }

        fun addLayer(layer: Layer): Builder {
            layers.add(layer)
            return this
        }

        fun create(): NeuralNetwork {
            return NeuralNetwork(this)
        }
    }

    // -----------------------------
    class NetworkState(network: NeuralNetwork) {
        var costFunction: String = network.costFunction.getName()
        var layers: Array<Layer.LayerState?> = arrayOfNulls<Layer.LayerState>(network.layers.size)

        init {
            for (l in network.layers.indices) {
                layers[l] = network.layers[l].state
            }
        }
    }
}

