package org.deep_thinker.verticles

import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import org.deep_thinker.model.*

class EnvironmentVerticle(private val envId: String, private val env: Environment<Int, FloatArray>, private val startEpisodeTopic: String, private val takeActionTopic: String, val getFirstActionTopic: String, val getActionTopic: String, val episodeCompleteTopic: String) : AbstractVerticle() {
  lateinit var bus: EventBus
  var episodeReward = 0.0f
  override fun start(startPromise: Promise<Void>) {
    println("env started $envId")

    bus = vertx.eventBus()
    bus.consumer(startEpisodeTopic, this::startEpisode)
    bus.consumer(takeActionTopic, this::takeAction)

    startPromise.complete()
  }

  private fun startEpisode(message: Message<StartEpisode>) {
    episodeReward = 0.0f
    val state = env.reset()
    bus.send(getFirstActionTopic, GetFirstAction(envId, state))
  }

  private fun takeAction(message: Message<TakeAction>) {
    val takeAction = message.body()

    val step = env.step(takeAction.action)
    episodeReward += step.reward

    if (!step.done) {
      bus.send(getActionTopic, GetAction(envId, step.state, step.reward))
    } else {
      bus.send(episodeCompleteTopic, EpisodeComplete(envId, step.state, step.reward, episodeReward))
      bus.publish("EpisodeComplete", EpisodeComplete(envId, step.state, step.reward, episodeReward))
    }
  }
}
