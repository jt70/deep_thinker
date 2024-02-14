package org.deep_thinker.agent.dqn

import ai.djl.engine.Engine
import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDList
import ai.djl.ndarray.NDManager
import ai.djl.training.GradientCollector
import ai.djl.training.ParameterStore
import ai.djl.training.loss.L2Loss
import ai.djl.training.optimizer.Adam
import ai.djl.training.optimizer.Optimizer
import com.google.flatbuffers.FloatVector
import org.deep_thinker.model.*
import org.deep_thinker.replay.ReplayBuffer
import kotlin.math.max
import kotlin.random.Random


class DeepQLearningDJL(config: DQNConfigFlat) {
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
    private val optimizer: Adam =
        Optimizer.adam().optLearningRateTracker(ai.djl.training.tracker.Tracker.fixed(config.learningRate())).build()
    private var globalStep: Int = 0
    private val mainManager: NDManager = NDManager.newBaseManager()
//    private val qNetworkManager: NDManager = mainManager.newSubManager()
//    private var targetNetworkManager: NDManager = mainManager.newSubManager()
    //private val qNetwork: Model = Model.newInstance("qNetwork")
//    private val targetNetwork: Model = Model.newInstance("targetNetwork")

    private val qNet = DeepQNetworkDJL(
        mainManager,
        "qNetwork",
        config.numInputs(),
        config.numActions(),
        intArrayOf(config.hidden1Size(), config.hidden2Size())
    )
    private val targetNet = DeepQNetworkDJL(
        mainManager,
        "targetNetwork",
        config.numInputs(),
        config.numActions(),
        intArrayOf(config.hidden1Size(), config.hidden2Size())
    )

    init {
//        val net = Mlp(config.numInputs(), config.numActions(), intArrayOf(config.hidden1Size(), config.hidden2Size()))
//        net.initialize(qNetworkManager, DataType.FLOAT32, Shape(config.numInputs().toLong()))
//        qNetwork.block = net

//        val t = Mlp(config.numInputs(), config.numActions(), intArrayOf(config.hidden1Size(), config.hidden2Size()))
//        t.initialize(targetNetworkManager, DataType.FLOAT32, Shape(config.numInputs().toLong()))
//        targetNetwork.block = t
//
        syncNets()
    }

    private fun printCollector() {
//        Engine.getInstance().newGradientCollector().use { collector ->
//            println(collector)
//        }
    }

    //var predictor = qNetwork.newPredictor(NoopTranslator());
//    var targetPredictor = targetNetwork.newPredictor(NoopTranslator());


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
        val (prevObs, prevAction) = previousObservation[episodeComplete.envId()]!!
        val state = getFloatArray(episodeComplete.stateVector(), episodeComplete.stateLength())
        replayBuffer.add(prevObs, prevAction, episodeComplete.reward(), true, state)
        previousObservation.remove(episodeComplete.envId())

        globalStep++

        checkTraining()
    }

    private fun checkTraining() {
        if (globalStep > learningStarts && globalStep % trainFrequency == 0) {
            val sample = replayBuffer.sample(batchSize)

            val states = sample.states
            val actions = sample.actions
            val rewards = sample.rewards
            val dones = sample.dones
            val nextStates = sample.nextStates

            mainManager.newSubManager().use { manager ->
                val tdTarget = getTdTarget(manager, nextStates, dones, rewards)

                // gradient descent
                gradientDescent(manager, states, actions, tdTarget)
            }

            if (globalStep % targetNetworkFrequency == 0) {
                syncNets()
            }
        }
    }

    private fun gradientDescent(
        manager: NDManager,
        states: Array<FloatArray>,
        actions: IntArray,
        tdTarget: NDArray?
    ) {
        Engine.getInstance().newGradientCollector().use { collector ->
            val loss = getLoss(manager, states, actions, tdTarget)
            collector.backward(loss)

            updateParams(collector)
        }
    }

    private fun getLoss(
        manager: NDManager,
        states: Array<FloatArray>,
        actions: IntArray,
        tdTarget: NDArray?
    ): NDArray? {
        val ps = ParameterStore(manager, false)

        val obsOutput: NDArray = qNet.forward(ps, NDList(manager.create(states))).singletonOrThrow()
        val actionsTensor = manager.create(actions).reshape(batchSize.toLong(), 1)

        val oldVal = obsOutput.gather(actionsTensor, 1).squeeze()
        val loss = lossFunc.evaluate(NDList(oldVal), NDList(tdTarget))
        return loss
    }

    private fun updateParams(collector: GradientCollector) {
        for (params in qNet.getParameters()) {
            val paramsArr = params.value.getArray()
            optimizer.update(params.key, paramsArr, paramsArr.gradient)
        }
        qNet.zeroGradients()
        //collector.zeroGradients()
    }

    private fun getTdTarget(
        manager: NDManager,
        nextStates: Array<FloatArray>,
        dones: FloatArray,
        rewards: FloatArray
    ): NDArray? {
        val output: NDArray =
            targetNet.predict(NDList(manager.create(nextStates))).singletonOrThrow().duplicate()

        val targetMax = output.max(intArrayOf(1))
        val donesTensor = manager.create(dones).flatten()
        val ones = manager.ones(donesTensor.shape)
        val tdTarget = manager.create(rewards).flatten()
            .add(manager.create(gamma).mul(targetMax).mul(ones.sub(donesTensor)))
        return tdTarget
    }

    private fun linearSchedule(startE: Float, endE: Float, duration: Float, t: Int): Float {
        val slope = (endE - startE) / duration
        return max(endE, slope * t + startE)
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
        val epsilon = getEpsilon(globalStep)
        val r = Random.nextDouble()
        return if (r < epsilon) {
            Random.nextInt(numActions)
        } else {
            printCollector()
            mainManager.newSubManager().use { manager ->
                val score: NDArray = qNet.predict(NDList(manager.create(state))).singletonOrThrow()
                score.argMax().getLong().toInt()
            }
        }
    }

    protected fun syncNets() {
        targetNet.copyParams(qNet)
    }

    fun close() {
        mainManager.close()
    }
}