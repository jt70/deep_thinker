package org.deep_thinker.agent.dqn.deep_thinker_dl

import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDManager
import com.google.flatbuffers.FloatVector
import org.deep_thinker.dl.math.Vec
import org.deep_thinker.model.DQNConfigFlat
import org.deep_thinker.model.EpisodeCompleteFlat
import org.deep_thinker.model.GetActionFlat
import org.deep_thinker.model.GetFirstActionFlat
import org.deep_thinker.replay.ReplayBuffer
import kotlin.math.max
import kotlin.random.Random


class DeepQLearning(config: DQNConfigFlat) {
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
    private var globalStep: Int = 0
    private val qNet = DeepQNetwork(
        config.numInputs(),
        config.numActions(),
        intArrayOf(config.hidden1Size(), config.hidden2Size()),
        config.learningRate()
    )
    private val targetNet = DeepQNetwork(
        config.numInputs(),
        config.numActions(),
        intArrayOf(config.hidden1Size(), config.hidden2Size()),
        config.learningRate()
    )

    init {
        syncNets()
    }

    fun getFirstAction(message: GetFirstActionFlat): Int {
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

            for (i in 0 until batchSize) {
                val state = states[i]
                val action = actions[i]
                val reward = rewards[i]
                val done = dones[i]
                val nextState = nextStates[i]

                val tdTarget: Vec = getTdTarget(nextState, done, reward)

                qNet.train(state, tdTarget)
            }
            qNet.network.updateFromLearning()

            if (globalStep % targetNetworkFrequency == 0) {
                syncNets()
            }
        }
    }

    private fun getTdTarget(nextState: FloatArray, done: Float, reward: Float): Vec {
        return Vec()
    }

//    private fun getTdTarget(
//        nextStates: Array<FloatArray>,
//        dones: FloatArray,
//        rewards: FloatArray
//    ): NDArray? {
//        val output: NDArray =
//            targetNet.predict(NDList(manager.create(nextStates))).singletonOrThrow().duplicate()
//
//        val targetMax = output.max(intArrayOf(1))
//        val donesTensor = manager.create(dones).flatten()
//        val ones = manager.ones(donesTensor.shape)
//        val tdTarget = manager.create(rewards).flatten()
//            .add(manager.create(gamma).mul(targetMax).mul(ones.sub(donesTensor)))
//        return tdTarget
//    }

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
            val results = qNet.evaluate(state)
            results.maxIndex()
        }
    }

    protected fun syncNets() {
        targetNet.copyParams(qNet)
    }
}