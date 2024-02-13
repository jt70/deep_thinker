package org.deep_thinker.agent.dqn

import ai.djl.Model
import ai.djl.basicmodelzoo.basic.Mlp
import ai.djl.engine.Engine
import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDList
import ai.djl.ndarray.NDManager
import ai.djl.ndarray.types.DataType
import ai.djl.ndarray.types.Shape
import ai.djl.training.loss.L2Loss
import ai.djl.training.optimizer.Adam
import ai.djl.training.optimizer.Optimizer
import ai.djl.translate.NoopTranslator
import com.google.flatbuffers.FloatVector
import org.deep_thinker.model.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import kotlin.random.Random

class DeepQLearningDJL2(config: DQNConfigFlat) {
    private val numActions: Int = config.numActions()
    private val totalTimesteps: Int = config.totalTimesteps()
    private val explorationFraction: Float = config.explorationFraction()
    private val endEpsilon: Float = config.endEpsilon()
    private val startEpsilon: Float = config.startEpsilon()
    private val targetNetworkFrequency: Int = config.targetNetworkFrequency()
    private val gamma: Float = config.gamma()
    private val batchSize: Int = config.batchSize()
    private val trainFrequency: Int = config.trainFrequency()
    private val learningStarts: Int = config.learningStarts()
    private val previousObservation: HashMap<String, Pair<FloatArray, Int>> = HashMap()
    private val replayBuffer: ReplayBuffer = ReplayBuffer(config.replayBufferSize(), config.numInputs())
    private val lossFunc: L2Loss = L2Loss()
    private val optimizer: Adam = Optimizer.adam().optLearningRateTracker(ai.djl.training.tracker.Tracker.fixed(config.learningRate())).build()
    private var globalStep: Int = 0
    private val mainManager: NDManager = NDManager.newBaseManager()
    private val qNetworkManager: NDManager = mainManager.newSubManager()
    private var targetNetworkManager: NDManager = mainManager.newSubManager()
    private val qNetwork: Model = Model.newInstance("qNetwork")
    private val targetNetwork: Model = Model.newInstance("targetNetwork")

    init {
        printCollector()

        val net = Mlp(config.numInputs(), config.numActions(), intArrayOf(config.hidden1Size(), config.hidden2Size()))
        net.initialize(qNetworkManager, DataType.FLOAT32, Shape(config.numInputs().toLong()))
        qNetwork.block = net

        printCollector()

        val t = Mlp(config.numInputs(), config.numActions(), intArrayOf(config.hidden1Size(), config.hidden2Size()))
        t.initialize(targetNetworkManager, DataType.FLOAT32, Shape(config.numInputs().toLong()))
        targetNetwork.block = t

        printCollector()

        syncNets()

        printCollector()
    }

    private fun printCollector() {
//        Engine.getInstance().newGradientCollector().use { collector ->
//            println(collector)
//        }
    }

    var predictor = qNetwork.newPredictor(NoopTranslator());
    var targetPredictor = targetNetwork.newPredictor(NoopTranslator());


    fun getFirstAction(message: GetFirstActionFlat): Int {
        printCollector()
        val envId = message.envId()
        val obs = getFloatArray(message.stateVector(), message.stateLength())
        val action = selectAction(globalStep, obs)

        previousObservation[envId] = Pair(obs, action)

        globalStep++

        checkTraining()
        return action
    }

    private fun getFloatArray(vector: FloatVector, length: Int): FloatArray {
        val array = FloatArray(length)
        for (i in 0 until length) {
            array[i] = vector.get(i)
        }
        return array
    }

    fun saveModel(message: SaveModel) {
//        val paramFile = File(message.path)
//        val os = DataOutputStream(Files.newOutputStream(paramFile.toPath()))
//        qNetwork.getBlock().saveParameters(os)
//        os.close()
    }

    fun getAction(message: GetActionFlat): Int {
        val envId = message.envId()
        val state = getFloatArray(message.stateVector(), message.stateLength())

        val (prevState, prevAction) = previousObservation[envId]!!
        replayBuffer.add(prevState, prevAction, message.reward(), false, state)

        val action = selectAction(globalStep, state)

        previousObservation[envId] = Pair(state, action)
        globalStep++

        checkTraining()
        return action
    }

    fun episodeComplete(episodeComplete: EpisodeCompleteFlat) {
        var (prevObs, prevAction) = previousObservation[episodeComplete.envId()]!!
        var state = getFloatArray(episodeComplete.stateVector(), episodeComplete.stateLength())
        replayBuffer.add(prevObs, prevAction, episodeComplete.reward(), true, state)
        previousObservation.remove(episodeComplete.envId())

        globalStep++

        checkTraining()
    }

    private fun checkTraining() {
        if (globalStep > learningStarts && globalStep % trainFrequency == 0) {
            var sample = replayBuffer.sample(batchSize);

            var states = sample.states
            var actions = sample.actions
            var rewards = sample.rewards
            var dones = sample.dones
            var nextStates = sample.nextStates

            mainManager.newSubManager().use { manager ->

                val output: NDArray =
                    targetPredictor.predict(NDList(manager.create(nextStates))).singletonOrThrow().duplicate()

                val targetMax = output.max(intArrayOf(1))
                val donesTensor = manager.create(dones).flatten()
                val ones = manager.ones(donesTensor.shape)
                val tdTarget = manager.create(rewards).flatten()
                    .add(manager.create(gamma).mul(targetMax).mul(ones.sub(donesTensor)))

                val obsOutput: NDArray = predictor.predict(NDList(manager.create(states))).singletonOrThrow()

                val actionsTensor = manager.create(actions).reshape(batchSize.toLong(), 1)

                val oldVal = obsOutput.gather(actionsTensor, 1).squeeze()
                val loss = lossFunc.evaluate(NDList(oldVal), NDList(tdTarget))
                gradientUpdate(loss)
            }

            if (globalStep % targetNetworkFrequency == 0) {
                syncNets()
            }
        }
    }

    protected fun gradientUpdate(loss: NDArray) {
        Engine.getInstance().newGradientCollector().use { collector ->
            collector.backward(loss)
            for (params in qNetwork.getBlock().getParameters()) {
                val params_arr = params.value.getArray()
                optimizer.update(params.key, params_arr, params_arr.gradient)
            }
        }
    }

    private fun linearSchedule(startE: Float, endE: Float, duration: Float, t: Int): Float {
        var slope = (endE - startE) / duration
        return Math.max(endE, slope * t + startE)
    }

    fun getEpsilon(t: Int): Float {
        return linearSchedule(
            startEpsilon,
            endEpsilon,
            explorationFraction * totalTimesteps,
            t
        )
    }

    private fun selectAction(globalStep: Int, state: FloatArray): Int {
        var epsilon = getEpsilon(globalStep)
        var r = Random.nextDouble()
        return if (r < epsilon) {
            Random.nextInt(numActions)
        } else {
            printCollector()
            mainManager.newSubManager().use { manager ->
                val score: NDArray = predictor.predict(NDList(manager.create(state))).singletonOrThrow()
                score.argMax().getLong().toInt()
            }
        }
    }

    protected fun syncNets() {
        val baos = ByteArrayOutputStream()
        val os = DataOutputStream(baos)
        qNetwork.getBlock().saveParameters(os)

        val bais = ByteArrayInputStream(baos.toByteArray())
        targetNetworkManager.close()
        targetNetworkManager = mainManager.newSubManager()
        targetNetwork.block.loadParameters(targetNetworkManager, DataInputStream(bais))

        targetPredictor = targetNetwork.newPredictor(NoopTranslator())
    }

    fun close() {
        mainManager.close()
    }
}