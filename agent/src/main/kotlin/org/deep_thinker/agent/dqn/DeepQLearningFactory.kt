package org.deep_thinker.agent.dqn

import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import org.deep_thinker.serde.DQNConfigSerde
import org.msgpack.core.MessagePack

class DeepQLearningFactory : AbstractVerticle() {
    val agents = mutableSetOf<String>()
    val dqnConfigSerde = DQNConfigSerde()
    private lateinit var bus: EventBus
    override fun start() {
        bus = vertx.eventBus()
        bus.consumer("createDQNAgent", this::createDQNAgent)

        println("DeepQLearningFactory started")
    }

    private fun createDQNAgent(message: Message<ByteArray>) {
        val unpacker = MessagePack.newDefaultUnpacker(message.body())
        val responseTopic = unpacker.unpackString()
        val responseId = unpacker.unpackString()
        val agentId = unpacker.unpackString()
        val config = dqnConfigSerde.deserialize(unpacker)

        if (agents.contains(agentId)) {
            val response = MessagePack.newDefaultBufferPacker()
            response
                .packString(responseId)
                .packString("Agent already exists")
            response.close()
            bus.send(responseTopic, response.toByteArray())
            return
        }

    }
}