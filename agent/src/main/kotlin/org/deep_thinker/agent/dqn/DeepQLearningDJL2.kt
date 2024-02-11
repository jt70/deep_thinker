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
import ai.djl.training.optimizer.Optimizer
import ai.djl.translate.NoopTranslator
import org.deep_thinker.model.*
import java.io.*
import kotlin.random.Random

class DeepQLearningDJL2(val config: DQNConfig) : DeepQLearning {
    private val previousObservation = HashMap<String, Pair<FloatArray, Int>>()
    private val replayBuffer = ReplayBuffer(config.replayBufferSize, config.numInputs)
    val lossFunc = L2Loss()
    val optimizer =
        Optimizer.adam().optLearningRateTracker(ai.djl.training.tracker.Tracker.fixed(config.learningRate)).build()
    private var globalStep = 0
    private val mainManager = NDManager.newBaseManager()
    val qNetworkManager = mainManager.newSubManager()
    var targetNetworkManager = mainManager.newSubManager()
    val qNetwork = Model.newInstance("qNetwork")
    val targetNetwork = Model.newInstance("targetNetwork")

    init {
        val net = Mlp(config.numInputs, config.numActions, intArrayOf(config.hidden1Size, config.hidden2Size))
        net.initialize(qNetworkManager, DataType.FLOAT32, Shape(config.numInputs.toLong()))
        qNetwork.block = net

        val t = Mlp(config.numInputs, config.numActions, intArrayOf(config.hidden1Size, config.hidden2Size))
        t.initialize(targetNetworkManager, DataType.FLOAT32, Shape(config.numInputs.toLong()))
        targetNetwork.block = t

        syncNets()
    }

    var predictor = qNetwork.newPredictor(NoopTranslator());
    var targetPredictor = targetNetwork.newPredictor(NoopTranslator());


    override fun getFirstAction(message: GetFirstAction): Int {
        val envId = message.envId
        val obs = message.state
        val action = selectAction(globalStep, obs)

        previousObservation[envId] = Pair(obs, action)

        globalStep++

        checkTraining()
        return action
    }

    override fun saveModel(message: SaveModel) {
//        val paramFile = File(message.path)
//        val os = DataOutputStream(Files.newOutputStream(paramFile.toPath()))
//        qNetwork.getBlock().saveParameters(os)
//        os.close()
    }

    override fun getAction(message: GetAction): Int {
        val envId = message.envId
        val state = message.state

        val (prevState, prevAction) = previousObservation[envId]!!
        replayBuffer.add(prevState, prevAction, message.reward, false, state)

        val action = selectAction(globalStep, state)

        previousObservation[envId] = Pair(state, action)
        globalStep++

        checkTraining()
        return action
    }

    override fun episodeComplete(episodeComplete: EpisodeComplete) {
        var (prevObs, prevAction) = previousObservation[episodeComplete.envId]!!
        replayBuffer.add(prevObs, prevAction, episodeComplete.reward, true, episodeComplete.state)
        previousObservation.remove(episodeComplete.envId)

        globalStep++

        checkTraining()
    }

    private fun checkTraining() {
        if (globalStep > config.learningStarts && globalStep % config.trainFrequency == 0) {
            var sample = replayBuffer.sample(config.batchSize);

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
                    .add(manager.create(config.gamma).mul(targetMax).mul(ones.sub(donesTensor)))

                val obsOutput: NDArray = predictor.predict(NDList(manager.create(states))).singletonOrThrow()

                val actionsTensor = manager.create(actions).reshape(config.batchSize.toLong(), 1)
                val oldVal = obsOutput.gather(actionsTensor, 1).squeeze()

                val loss = lossFunc.evaluate(NDList(oldVal), NDList(tdTarget))
                gradientUpdate(loss)
            }

            if (globalStep % config.targetNetworkFrequency == 0) {
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
            config.startEpsilon,
            config.endEpsilon,
            config.explorationFraction * config.totalTimesteps,
            t
        )
    }

    private fun selectAction(globalStep: Int, state: FloatArray): Int {
        var epsilon = getEpsilon(globalStep)
        var r = Random.nextDouble()
        return if (r < epsilon) {
            Random.nextInt(config.numActions)
        } else {
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

    override fun close() {
        mainManager.close()
    }
}