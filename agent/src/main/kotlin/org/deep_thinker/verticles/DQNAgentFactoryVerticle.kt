package org.deep_thinker.verticles

import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.eventbus.Message
import org.deep_thinker.agent.dqn.DeepQLearningAgentVerticle
import org.deep_thinker.agent.dqn.DeepQLearningDJL2
import org.deep_thinker.serde.DQNConfigSerde
import org.msgpack.core.MessagePack

class DQNAgentFactoryVerticle : AbstractVerticle() {
    val dqnConfigSerde = DQNConfigSerde()
    override fun start(startPromise: Promise<Void>) {
        println("started AgentFactoryVerticle")

        val bus = vertx.eventBus()
        bus.consumer("createDQNAgent", this::createDQNAgent)
        println("consuming createDQNAgent")

        startPromise.complete()
    }

    private fun createDQNAgent(message: Message<ByteArray>) {
        val unpacker = MessagePack.newDefaultUnpacker(message.body())
        val config = dqnConfigSerde.deserialize(unpacker)
        vertx.deployVerticle(DeepQLearningAgentVerticle(config))
        println("DQN agentCreated")

        message.reply(ByteArray(0))
    }
}