package org.deep_thinker.agent.dqn.deep_thinker_dl

import org.deep_thinker.dl.activation.Activation
import org.deep_thinker.dl.cost.MSE
import org.deep_thinker.dl.initializer.XavierNormal
import org.deep_thinker.dl.math.Vec
import org.deep_thinker.dl.network.Layer
import org.deep_thinker.dl.network.NeuralNetwork
import org.deep_thinker.dl.optimizer.GradientDescent

class DeepQNetwork(
    numInputs: Int,
    numActions: Int,
    hiddenSizes: IntArray,
    learningRate: Float
) {
    val network = NeuralNetwork.Builder(numInputs)
            .addLayer(Layer(hiddenSizes[0], Activation.ReLU, 0.5))
            .addLayer(Layer(hiddenSizes[1], Activation.ReLU, 0.5))
            .addLayer(Layer(numActions, Activation.Identity, 0.5))
            .setCostFunction(MSE())
            .setOptimizer(GradientDescent(learningRate.toDouble()))
            .initWeights(XavierNormal())
            .create()

    fun copyParams(sourceNetwork: DeepQNetwork) {
        network.copyParams(sourceNetwork.network)
    }

    fun evaluate(input: FloatArray): Vec {
        val vecInput = Vec(input)
        return network.evaluate(vecInput).getOutput()
    }

    fun train(state: FloatArray, tdTarget: Vec) {

    }
}
