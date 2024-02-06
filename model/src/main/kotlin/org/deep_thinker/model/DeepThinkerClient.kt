package org.deep_thinker.model

import java.util.concurrent.CompletableFuture

interface DeepThinkerClient {
    fun toUpperCase(s: String): CompletableFuture<String>
    fun createDQNAgent(agentId: String, config: DQNConfig): CompletableFuture<String>

    fun getFirstAction(agentId: String, envId: String, state: FloatArray): CompletableFuture<Int>

    fun getAction(agentId: String, envId: String, state: FloatArray, reward: Float): CompletableFuture<Int>

    fun episodeComplete(
        agentId: String, envId: String, state: FloatArray, reward: Float, episodeReward: Float
    ): CompletableFuture<Void>
}