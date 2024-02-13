package org.deep_thinker.agent.dqn

import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import org.deep_thinker.model.*

class DeepQLearningVerticle(val deepQLearning: DeepQLearningDJL, val topicGenerator: TopicGenerator) : AbstractVerticle() {
    private lateinit var bus: EventBus
    override fun start(startPromise: Promise<Void>) {
        println("DeepQLearningVerticle started")

        bus = vertx.eventBus()
        bus.consumer("dqn.GetFirstAction", this::getFirstAction)
        bus.consumer("dqn.GetAction", this::getAction)
        bus.consumer("EpisodeComplete", this::episodeComplete)
        bus.consumer("SaveModel", this::saveModel)

        startPromise.complete()
    }

    override fun stop(stopPromise: Promise<Void>) {
        println("DeepQLearningVerticle stopped")
        deepQLearning.close()
        stopPromise.complete()
    }

    private fun getFirstAction(m: Message<GetFirstAction>) {
        val message = m.body()
        deepQLearning.printCollector()
        val action = deepQLearning.getFirstAction(message)
        bus.send(topicGenerator.getTakeActionTopic(message.envId), TakeAction(action))
    }

    private fun getAction(m: Message<GetAction>) {
        val message = m.body()
        val action = deepQLearning.getAction(message)
        bus.send(topicGenerator.getTakeActionTopic(message.envId), TakeAction(action))
    }

    private fun episodeComplete(message: Message<EpisodeComplete>) {
        val episodeComplete = message.body()
        deepQLearning.episodeComplete(episodeComplete)
    }

    private fun saveModel(m: Message<SaveModel>) {
        val message = m.body()
        deepQLearning.saveModel(message)
        bus.publish("ModelSaved", true)
    }
}
