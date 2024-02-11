package org.deep_thinker.agent.dqn

import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import org.deep_thinker.model.*
import org.deep_thinker.serde.GetFirstActionFlatSerde
import org.deep_thinker.serde.IntFlatSerde

class DeepQLearningAgentVerticle(val config: DQNConfigFlat) : AbstractVerticle() {
    val intSerde = IntFlatSerde()
    val getFirstActionSerde = GetFirstActionFlatSerde()

    private lateinit var deepQLearning: DeepQLearningDJL2
    private lateinit var bus: EventBus
    override fun start(startPromise: Promise<Void>) {
        println("DeepQLearningAgentVerticle starting...")
        bus = vertx.eventBus()
        bus.consumer("getFirstAction.${config.agentId()}", this::getFirstAction)
        bus.consumer("getAction.${config.agentId()}", this::getAction)
        bus.consumer("episodeComplete.${config.agentId()}", this::episodeComplete)

        deepQLearning = DeepQLearningDJL2(config)
        println("DeepQLearningAgentVerticle started...")
        startPromise.complete()
    }

    private fun getFirstAction(m: Message<ByteArray>) {
        m.reply(intSerde.serialize(0))
    }

    private fun getAction(m: Message<ByteArray>) {
        m.reply(intSerde.serialize(0))
    }

    private fun episodeComplete(m: Message<ByteArray>) {
        m.reply(intSerde.serialize(0))
    }
}