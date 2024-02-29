package org.deep_thinker.agent.dqn.deep_thinker_dl

import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.eventbus.Message
import org.deep_thinker.serde.DQNConfigFlatSerde
import org.deep_thinker.serde.IntFlatSerde

class DQNAgentFactoryVerticle : AbstractVerticle() {
    val activeAgents = mutableSetOf<String>()
    val intSerde = IntFlatSerde()
    val dqnConfigSerde = DQNConfigFlatSerde()
    override fun start(startPromise: Promise<Void>) {
        println("started AgentFactoryVerticle")

        val bus = vertx.eventBus()
        bus.consumer("createDQNAgent", this::createDQNAgent)
        println("consuming createDQNAgent")

        startPromise.complete()
    }

    private fun createDQNAgent(message: Message<ByteArray>) {
        val config = dqnConfigSerde.deserialize(message.body())
        if (!activeAgents.contains(config.agentId())) {
            activeAgents.add(config.agentId())
            vertx.deployVerticle(DeepQLearningAgentVerticle(config))
            println("DQN agentCreated for id: ${config.agentId()}")
        } else {
            println("DQN agent already exists for id: ${config.agentId()}")
        }
        message.reply(intSerde.serialize(0))
    }
}