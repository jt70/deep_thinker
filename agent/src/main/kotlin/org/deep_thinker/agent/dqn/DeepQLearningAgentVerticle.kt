package org.deep_thinker.agent.dqn

import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import org.deep_thinker.model.*
import org.msgpack.core.MessagePack

class DeepQLearningAgentVerticle(val config: DQNConfig) : AbstractVerticle() {
    private lateinit var bus: EventBus
    override fun start(startPromise: Promise<Void>) {
        println("DeepQLearningAgentVerticle started")
        bus = vertx.eventBus()
        bus.consumer("getFirstAction.${config.agentId}", this::getFirstAction)
        bus.consumer("getAction.${config.agentId}", this::getAction)
        bus.consumer("episodeComplete.${config.agentId}", this::episodeComplete)

        println("DeepQLearningAgentVerticle finished start")
        startPromise.complete()
        println("promise completed")
    }

    private fun getFirstAction(m: Message<ByteArray>) {
        val packer = MessagePack.newDefaultBufferPacker()
        packer.packInt(0)
        packer.close()
        m.reply(packer.toByteArray())
    }

    private fun getAction(m: Message<ByteArray>) {
        val packer = MessagePack.newDefaultBufferPacker()
        packer.packInt(0)
        packer.close()
        m.reply(packer.toByteArray())
    }

    private fun episodeComplete(m: Message<ByteArray>) {
        m.reply(ByteArray(0))
    }
}