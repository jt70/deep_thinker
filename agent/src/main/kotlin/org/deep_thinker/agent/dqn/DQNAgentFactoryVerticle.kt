package org.deep_thinker.agent.dqn

import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.eventbus.Message
import org.deep_thinker.serde.DQNConfigFlatSerde
import org.deep_thinker.serde.IntFlatSerde

class DQNAgentFactoryVerticle : AbstractVerticle() {
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
        vertx.deployVerticle(DeepQLearningAgentVerticle(config))
        println("DQN agentCreated")

        message.reply(intSerde.serialize(0))
    }
}