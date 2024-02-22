package org.deep_thinker.dl.network

import org.deep_thinker.dl.activation.Activation.Companion.ReLU
import org.deep_thinker.dl.activation.Activation.Companion.Sigmoid
import org.deep_thinker.dl.activation.Softmax
import org.deep_thinker.dl.cost.HalfQuadratic
import org.deep_thinker.dl.cost.Quadratic
import org.deep_thinker.dl.initializer.InitializerFunction
import org.deep_thinker.dl.initializer.XavierNormal
import org.deep_thinker.dl.math.Matrix
import org.deep_thinker.dl.math.Vec
import org.deep_thinker.dl.optimizer.GradientDescent
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class NeuralNetworkTest {
    @Test
    fun testFFExampleFromBlog() {
        val initWeights = arrayOf(
            arrayOf(doubleArrayOf(0.3, 0.2), doubleArrayOf(-.4, 0.6)),
            arrayOf(doubleArrayOf(0.7, -.3), doubleArrayOf(0.5, -.1))
        )

        val network: NeuralNetwork =
            NeuralNetwork.Builder(2)
                .addLayer(Layer(2, Sigmoid, Vec(doubleArrayOf(0.25, 0.45))))
                .addLayer(Layer(2, Sigmoid, Vec(doubleArrayOf(0.15, 0.35))))
                .setCostFunction(Quadratic())
                .setOptimizer(GradientDescent(0.1))
                .initWeights(InitializerFunction { weights, layer ->
                    val data: Array<DoubleArray> = weights.data
                    for (row in data.indices) System.arraycopy(
                        initWeights[layer][row],
                        0,
                        data[row],
                        0,
                        data[0].size
                    )
                })
                .create()

        var out: Vec = network.evaluate(Vec(2, 3), Vec(doubleArrayOf(1.0, 0.2))).getOutput()

        var data: DoubleArray = out.data
        assertEquals(0.712257432295742, data[0], EPS)
        assertEquals(0.533097573871501, data[1], EPS)

        network.updateFromLearning()

        val result = network.evaluate(Vec(2, 3), Vec(doubleArrayOf(1.0, 0.2)))
        out = result.getOutput()
        data = out.data
        assertEquals(0.7192693606054349, data[0], EPS)
        assertEquals(0.5243093430032615, data[1], EPS)
    }

    @Test
    fun testEvaluate() {
        val initWeights = arrayOf(
            arrayOf(doubleArrayOf(0.1, 0.2, 0.3), doubleArrayOf(0.3, 0.2, 0.7), doubleArrayOf(0.4, 0.3, 0.9)),
            arrayOf(doubleArrayOf(0.2, 0.3, 0.5), doubleArrayOf(0.3, 0.5, 0.7), doubleArrayOf(0.6, 0.4, 0.8)),
            arrayOf(doubleArrayOf(0.1, 0.4, 0.8), doubleArrayOf(0.3, 0.7, 0.2), doubleArrayOf(0.5, 0.2, 0.9))
        )

        val network: NeuralNetwork =
            NeuralNetwork.Builder(3)
                .addLayer(Layer(3, ReLU, 1.0))
                .addLayer(Layer(3, Sigmoid, 1.0))
                .addLayer(Layer(3, Softmax(), 1.0))
                .initWeights(InitializerFunction { weights, layer ->
                    val data: Array<DoubleArray> = weights.data
                    for (row in data.indices) System.arraycopy(
                        initWeights[layer][row],
                        0,
                        data[row],
                        0,
                        data[0].size
                    )
                })
                .create()

        val out: Vec = network.evaluate(Vec(doubleArrayOf(0.1, 0.2, 0.7))).getOutput()

        val data: DoubleArray = out.data
        assertEquals(0.1984468942, data[0], EPS)
        assertEquals(0.2853555304, data[1], EPS)
        assertEquals(0.5161975753, data[2], EPS)
        assertEquals(1.0, data[0] + data[1] + data[2], EPS)
    }


    // Based on forward pass here
    // https://mattmazur.com/2015/03/17/a-step-by-step-backpropagation-example/
    @Test
    fun testEvaluateAndLearn() {
        val initWeights = arrayOf(
            arrayOf(doubleArrayOf(0.15, 0.25), doubleArrayOf(0.20, 0.30)),
            arrayOf(doubleArrayOf(0.40, 0.50), doubleArrayOf(0.45, 0.55)),
        )

        val network: NeuralNetwork =
            NeuralNetwork.Builder(2)
                .addLayer(Layer(2, Sigmoid, Vec(doubleArrayOf(0.35, 0.35))))
                .addLayer(Layer(2, Sigmoid, Vec(doubleArrayOf(0.60, 0.60))))
                .setCostFunction(HalfQuadratic())
                .setOptimizer(GradientDescent(0.5))
                .initWeights(InitializerFunction { weights, layer ->
                    val data: Array<DoubleArray> = weights.data
                    for (row in data.indices) System.arraycopy(
                        initWeights[layer][row],
                        0,
                        data[row],
                        0,
                        data[0].size
                    )
                })
                .create()


        val expected: Vec = Vec(doubleArrayOf(0.01, 0.99))
        val input: Vec = Vec(doubleArrayOf(0.05, 0.1))

        var result = network.evaluate(input, expected)

        var out: Vec = result.getOutput()

        assertEquals(0.29837110, result.cost!!, EPS)
        assertEquals(0.75136507, out.data.get(0), EPS)
        assertEquals(0.77292846, out.data.get(1), EPS)

        network.updateFromLearning()

        result = network.evaluate(input, expected)
        out = result.getOutput()

        assertEquals(0.27740248, result.cost!!, EPS)
        assertEquals(0.72453469, out.data.get(0), EPS)
        assertEquals(0.77965472, out.data.get(1), EPS)

        for (i in 0 until 10000 - 2) {
            network.updateFromLearning()
            result = network.evaluate(input, expected)
        }

        out = result.getOutput()
        assertEquals(0.000002448, result.cost!!, EPS)
        assertEquals(0.009999999, out.data.get(0), EPS)
        assertEquals(0.989999999, out.data.get(1), EPS)
    }

//    @Test
//    @org.junit.Ignore // todo test is intermittently failing. Look into this.
//    fun testEvaluateAndLearn2() {
//        val network: NeuralNetwork =
//            Builder(4)
//                .addLayer(Layer(6, Sigmoid, 0.5))
//                .addLayer(Layer(14, Sigmoid, 0.5))
//                .setCostFunction(Quadratic())
//                .setOptimizer(GradientDescent(1))
//                .initWeights(XavierNormal())
//                .create()
//
//
//        val trainInputs = arrayOf(
//            intArrayOf(1, 1, 1, 0),
//            intArrayOf(1, 1, 0, 0),
//            intArrayOf(0, 1, 1, 0),
//            intArrayOf(1, 0, 1, 0),
//            intArrayOf(1, 0, 0, 0),
//            intArrayOf(0, 1, 0, 0),
//            intArrayOf(0, 0, 1, 0),
//            intArrayOf(1, 1, 1, 1),
//            intArrayOf(1, 1, 0, 1),
//            intArrayOf(0, 1, 1, 1),
//            intArrayOf(1, 0, 1, 1),
//            intArrayOf(1, 0, 0, 1),
//            intArrayOf(0, 1, 0, 1),
//            intArrayOf(0, 0, 1, 1)
//        )
//
//        val trainOutput = arrayOf(
//            intArrayOf(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
//            intArrayOf(0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
//            intArrayOf(0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
//            intArrayOf(0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
//            intArrayOf(0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0),
//            intArrayOf(0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0),
//            intArrayOf(0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0),
//            intArrayOf(0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0),
//            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0),
//            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0),
//            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0),
//            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0),
//            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0),
//            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1)
//        )
//
//
//        var cnt = 0
//        for (i in 0..4499) {
//            val input: Vec = Vec(trainInputs[cnt])
//            val expected: Vec = Vec(trainOutput[cnt])
//            network.evaluate(input, expected)
//            network.updateFromLearning()
//            cnt = (cnt + 1) % trainInputs.size
//        }
//
//        for (i in trainInputs.indices) {
//            val result = network.evaluate(Vec(trainInputs[i]))
//            val ix = result.getOutput().indexOfLargestElement()
//            assertEquals(Vec(trainOutput[i]), Vec(trainOutput[ix]))
//        }
//    }

    @Test
    fun testMakeACopyOfNetwork() {
        val network1: NeuralNetwork =
            NeuralNetwork.Builder(4)
                .addLayer(Layer(6, Sigmoid, 0.5))
                .addLayer(Layer(14, Sigmoid, 0.5))
                .setCostFunction(Quadratic())
                .setOptimizer(GradientDescent(1.0))
                .initWeights(XavierNormal())
                .create()

        val network2: NeuralNetwork = NeuralNetwork.Builder(network1).create()

        val trainInputs = arrayOf(
            doubleArrayOf(1.0, 1.0, 1.0, 0.0),
            doubleArrayOf(1.0, 1.0, 0.0, 0.0),
            doubleArrayOf(0.0, 1.0, 1.0, 0.0),
            doubleArrayOf(1.0, 0.0, 1.0, 0.0),
        )

        val trainOutput = arrayOf(
            doubleArrayOf(1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
            doubleArrayOf(0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
            doubleArrayOf(0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
            doubleArrayOf(0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
        )


        var cnt = 0
        for (i in 0..1099) {
            val input: Vec = Vec(trainInputs[cnt])
            val expected: Vec = Vec(trainOutput[cnt])
            network1.evaluate(input, expected)
            network1.updateFromLearning()
            network2.evaluate(input, expected)
            network2.updateFromLearning()
            cnt = (cnt + 1) % trainInputs.size
        }

        for (trainInput in trainInputs) {
            val input: Vec = Vec(trainInput)
            val result1 = network1.evaluate(input)
            val result2 = network2.evaluate(input)
            val out1: Vec = result1.getOutput()
            val out2: Vec = result2.getOutput()
            assertEquals(out1, out2)
        }
    }

    @Test
    fun testBatching() {
        val network: NeuralNetwork =
            NeuralNetwork.Builder(2)
                .addLayer(Layer(2, ReLU, 0.5))
                .addLayer(Layer(2, Sigmoid, 0.5))
                .initWeights(XavierNormal())
                .create()

        val i1: Vec = Vec(doubleArrayOf(0.1, 0.7))
        val i2: Vec = Vec(doubleArrayOf(-0.2, 0.3))
        val w1: Vec = Vec(doubleArrayOf(0.2, 0.1))
        val w2: Vec = Vec(doubleArrayOf(1.2, -0.4))

        // First, evaluate without learning
        var out1a = network.evaluate(i1)
        var out2a = network.evaluate(i2)

        // Then this should do nothing to the net
        network.updateFromLearning()

        var out1b = network.evaluate(i1, w1)
        var out2b = network.evaluate(i2, w2)
        assertEquals(out1a.getOutput(), out1b.getOutput())
        assertEquals(out2a.getOutput(), out2b.getOutput())

        // Now, evaluate and learn
        out1a = network.evaluate(i1, w1)
        out2a = network.evaluate(i2, w2)

        val cost1BeforeLearning: Double? = out1b.cost
        val cost2BeforeLearning: Double? = out2b.cost

        // First verify that we still have not changed any weights
        assertEquals(out1a.getOutput(), out1b.getOutput())
        assertEquals(out2a.getOutput(), out2b.getOutput())

        // This should however change things ...
        network.updateFromLearning()

        out1b = network.evaluate(i1, w1)
        out2b = network.evaluate(i2, w2)

        assertNotEquals(out1a.getOutput(), out1b.getOutput())
        assertNotEquals(out2a.getOutput(), out2b.getOutput())

        // ... and the cost should be lower
        val cost1AfterLearning: Double? = out1b.cost
        val cost2AfterLearning: Double? = out2b.cost

        assertTrue(cost1AfterLearning!! < cost1BeforeLearning!!)
        assertTrue(cost2AfterLearning!! < cost2BeforeLearning!!)
    }


    @Test
    fun testExampleFromArticleSeries() {
        val network: NeuralNetwork =
            NeuralNetwork.Builder(2)
                .addLayer(Layer(2, Sigmoid, Vec(doubleArrayOf(0.25, 0.45))))
                .addLayer(Layer(2, Sigmoid, Vec(doubleArrayOf(0.15, 0.35))))
                .setOptimizer(GradientDescent(0.1))
                .initWeights(InitializerFunction { weights, layer ->
                    when (layer) {
                        0 -> weights.fillFrom(
                            Matrix(
                                arrayOf<DoubleArray>(
                                    doubleArrayOf(.3, .2),
                                    doubleArrayOf(-.4, .6)
                                )
                            )
                        )

                        1 -> weights.fillFrom(
                            Matrix(
                                arrayOf<DoubleArray>(
                                    doubleArrayOf(.7, -.3),
                                    doubleArrayOf(.5, -0.1)
                                )
                            )
                        )

                        else -> throw IllegalStateException("Not known layer!")
                    }
                })
                .create()

        System.out.println("Network before first learning " + network.toJson(true))

        var evaluate = network.evaluate(Vec(2, 3), Vec(doubleArrayOf(1.0, 0.2)))

        println("Evaluate 1: $evaluate")

        network.updateFromLearning()

        System.out.println("Network after first learning " + network.toJson(true))

        evaluate = network.evaluate(Vec(2, 3), Vec(doubleArrayOf(1.0, 0.2)))

        println("Evaluate 2: $evaluate")
    }

    companion object {
        private const val EPS = 0.00001
    }
}
