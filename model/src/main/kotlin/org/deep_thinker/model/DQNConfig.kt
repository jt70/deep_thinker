package org.deep_thinker.model

data class DQNConfig(
    val startEpsilon: Float,
    val endEpsilon: Float,
    val explorationFraction: Float,
    val learningStarts: Int,
    val trainFrequency: Int,
    val batchSize: Int,
    val learningRate: Float,
    val targetNetworkFrequency: Int,
    val gamma: Float,
    val numInputs: Int,
    val numActions: Int,
    val hidden1Size: Int,
    val hidden2Size: Int,
    val totalTimesteps: Int,
    val replayBufferSize: Int
)

