package org.deep_thinker.experiment

import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import org.deep_thinker.model.EpisodeComplete
import org.deep_thinker.model.SaveModel
import org.deep_thinker.model.TopicGenerator
import org.deep_thinker.model.StartEpisode

class ExperimentVerticle(private val topicGenerator: TopicGenerator, private val envIds: List<String>, private var episodesRemaining: Int, private val modelPath:String) : AbstractVerticle() {
  lateinit var bus: EventBus
  var envTopics: Map<String, String> = mapOf()

  override fun start(startPromise: Promise<Void>) {
    println("experiment started")

    bus = vertx.eventBus()
    bus.consumer("EpisodeComplete", this::episodeComplete)

    startPromise.complete()

    envTopics = envIds.map { envId ->
      Pair(envId, topicGenerator.getStartEpisodeTopic(envId))
    }.toMap()

    for (envId in envIds) {
      episodesRemaining -= 1
      bus.send(envTopics[envId]!!, StartEpisode(episodesRemaining))
    }
  }

  override fun stop(stopPromise: Promise<Void>) {
    println("experiment stopped")
    stopPromise.complete()
  }

  private fun episodeComplete(message: Message<EpisodeComplete>) {
    val episodeComplete = message.body()

    episodesRemaining -= 1
    if (episodesRemaining > 0) {
      bus.send(envTopics[episodeComplete.envId]!!, StartEpisode(episodesRemaining))
    }
    if (episodesRemaining == 0) {
      println("model saved")
      bus.send("SaveModel", SaveModel(modelPath))
      getVertx().undeploy(deploymentID())
    }

    if (episodesRemaining % 100 == 0) {
      println("Env id ${episodeComplete.envId}, Episode $episodesRemaining, Reward ${episodeComplete.episodeReward}")
    }
  }
}
