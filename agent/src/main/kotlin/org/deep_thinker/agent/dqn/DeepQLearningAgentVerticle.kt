package org.deep_thinker.agent.dqn

import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import org.deep_thinker.model.DQNConfigFlat
import org.deep_thinker.serde.EpisodeCompleteFlatSerde
import org.deep_thinker.serde.GetActionFlatSerde
import org.deep_thinker.serde.GetFirstActionFlatSerde
import org.deep_thinker.serde.IntFlatSerde

class DeepQLearningAgentVerticle(private val config: DQNConfigFlat) : AbstractVerticle() {
    private val intSerde = IntFlatSerde()
    private val getFirstActionSerde = GetFirstActionFlatSerde()
    private val getActionSerde = GetActionFlatSerde()
    private val episodeCompleteSerde = EpisodeCompleteFlatSerde()
    private lateinit var deepQLearning: DeepQLearningDJL
    private lateinit var bus: EventBus

    override fun start(startPromise: Promise<Void>) {
        println("DeepQLearningAgentVerticle starting...")
        bus = vertx.eventBus()
        bus.consumer("getFirstAction.${config.agentId()}", this::getFirstAction)
        bus.consumer("getAction.${config.agentId()}", this::getAction)
        bus.consumer("episodeComplete.${config.agentId()}", this::episodeComplete)

        deepQLearning = DeepQLearningDJL(config)
        println("DeepQLearningAgentVerticle started...")
        startPromise.complete()
    }

    private fun getFirstAction(m: Message<ByteArray>) {
        val getFirstAction = getFirstActionSerde.deserialize(m.body())
        val action = deepQLearning.getFirstAction(getFirstAction)
        m.reply(intSerde.serialize(action))
    }

    private fun getAction(m: Message<ByteArray>) {
        val getAction = getActionSerde.deserialize(m.body())
        val action = deepQLearning.getAction(getAction)
        m.reply(intSerde.serialize(action))
    }

    private fun episodeComplete(m: Message<ByteArray>) {
        val episodeComplete = episodeCompleteSerde.deserialize(m.body())
        deepQLearning.episodeComplete(episodeComplete)
        m.reply(intSerde.serialize(0))
    }
}
