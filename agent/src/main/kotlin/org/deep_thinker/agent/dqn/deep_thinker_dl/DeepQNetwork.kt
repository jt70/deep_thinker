package org.deep_thinker.agent.dqn.deep_thinker_dl

import ai.djl.Model
import ai.djl.basicmodelzoo.basic.Mlp
import ai.djl.ndarray.NDList
import ai.djl.ndarray.NDManager
import ai.djl.ndarray.types.DataType
import ai.djl.ndarray.types.Shape
import ai.djl.nn.ParameterList
import ai.djl.training.ParameterStore
import ai.djl.translate.NoopTranslator
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

class DeepQNetwork(
    val parentManager: NDManager,
    name: String,
    numInputs: Int,
    numActions: Int,
    hiddenSizes: IntArray
) {
    val model: Model = Model.newInstance(name)
    var manager = parentManager.newSubManager()

    init {
        val net = Mlp(numInputs, numActions, hiddenSizes)
        net.initialize(manager, DataType.FLOAT32, Shape(numInputs.toLong()))
        model.block = net
    }

    var predictor = model.newPredictor(NoopTranslator())

    fun predict(ndList: NDList): NDList {
        return predictor.predict(ndList)
    }

    fun forward(ps: ParameterStore, ndList: NDList): NDList {
        return model.block.forward(ps, ndList, true)
    }

    fun getParameters(): ParameterList {
        return model.block.parameters
    }

    fun copyParams(sourceNetwork: DeepQNetwork) {
        val bytes: ByteArray = sourceNetwork.serializeParams()
        val bais = ByteArrayInputStream(bytes)

        manager.close()
        manager = parentManager.newSubManager()
        model.block.loadParameters(manager, DataInputStream(bais))

        predictor = model.newPredictor(NoopTranslator())
    }

    private fun serializeParams(): ByteArray {
        val baos = ByteArrayOutputStream()
        val os = DataOutputStream(baos)
        model.block.saveParameters(os)
        return baos.toByteArray()
    }

    fun zeroGradients() {
        for (array in manager.managedArrays) {
            if (array.hasGradient()) {
                array.gradient.subi(array.gradient)
            }
        }
    }
}
