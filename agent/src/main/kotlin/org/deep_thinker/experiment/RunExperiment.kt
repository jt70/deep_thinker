package org.deep_thinker.experiment

import ai.djl.Model
import ai.djl.basicmodelzoo.basic.Mlp
import ai.djl.inference.Predictor
import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDList
import ai.djl.ndarray.NDManager
import ai.djl.ndarray.types.DataType
import ai.djl.ndarray.types.Shape
import ai.djl.translate.NoopTranslator
import io.vertx.core.*
import io.vertx.core.eventbus.EventBus
import org.deep_thinker.agent.dqn.DeepQLearningDJL
import org.deep_thinker.agent.dqn.DeepQLearningVerticle
import org.deep_thinker.model.*
import org.deep_thinker.org.deep_thinker.codec.*
import org.deep_thinker.verticles.EnvironmentVerticle
import java.io.DataInputStream
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.CompletableFuture

fun runDQNExperiment(
    config: DQNConfig,
    numEnvironments: Int,
    totalEpisodes: Int,
    modelPath: String,
    createEnv: () -> Environment<Int, FloatArray>
) {
    val vertx: Vertx = Vertx.vertx(VertxOptions().setBlockedThreadCheckInterval(1000 * 60 * 60))
    val eventBus = vertx.eventBus()

    registerCodecs(eventBus)

    val topicGenerator = TopicGenerator()

    // TODO: fix this
    val modelSavedFuture = CompletableFuture<Void>()
    vertx.eventBus().consumer<Boolean>("ModelSaved") { _ ->
        modelSavedFuture.complete(null)
        println("run experiment model saved")
    }

    val startTime = System.currentTimeMillis()

    val deepQLearning = DeepQLearningDJL(config)

    vertx.deployVerticle(DeepQLearningVerticle(deepQLearning, topicGenerator)).onComplete { res: AsyncResult<String> ->
        if (res.succeeded()) {
            println("Deployment id is: " + res.result())

            val envIdFutures = (0 until numEnvironments).map { _ ->
                val envId = UUID.randomUUID().toString()
                Pair(
                    envId,
                    vertx.deployVerticle(
                        EnvironmentVerticle(
                            envId,
                            createEnv(),
                            topicGenerator.getStartEpisodeTopic(envId),
                            topicGenerator.getTakeActionTopic(envId),
                            "dqn.GetFirstAction",
                            "dqn.GetAction",
                            topicGenerator.getEpisodeCompleteTopic(envId)
                        )
                    )
                )
            }

            val envFutures = envIdFutures.map { (_, future) ->
                future
            }

            val envIds = envIdFutures.map { (envId, _) ->
                envId
            }

            Future.all(envFutures).onComplete { _: AsyncResult<CompositeFuture> ->
                vertx.deployVerticle(ExperimentVerticle(topicGenerator, envIds, totalEpisodes, modelPath))
            }
        } else {
            println("Deployment failed!")
        }
    }

    Thread.sleep(1000)
    deepQLearning.printCollector()

    // wait for model to be saved
    modelSavedFuture.get()
    val endTime = System.currentTimeMillis()
    println("single-threaded agent training time (ms): ${endTime - startTime}")

    testModel(config, modelPath, createEnv)
}

private fun registerCodecs(eventBus: EventBus) {
    eventBus.registerDefaultCodec(StartEpisode::class.java, StartEpisodeCodec())
    eventBus.registerDefaultCodec(GetFirstAction::class.java, GetFirstActionCodec())
    eventBus.registerDefaultCodec(GetAction::class.java, GetActionCodec())
    eventBus.registerDefaultCodec(EpisodeComplete::class.java, EpisodeCompleteCodec())
    eventBus.registerDefaultCodec(TakeAction::class.java, TakeActionCodec())
    eventBus.registerDefaultCodec(SaveModel::class.java, SaveModelCodec())
    eventBus.registerDefaultCodec(ActionPerformed::class.java, ActionPerformedCodec())
    eventBus.registerDefaultCodec(FirstActionPerformed::class.java, FirstActionPerformedCodec())
}

fun getAction(manager: NDManager, predictor: Predictor<NDList, NDList>, floatArrayOf: FloatArray): Int {
    return manager.newSubManager().use { m ->
        val score: NDArray = predictor.predict(NDList(m.create(floatArrayOf))).singletonOrThrow()
        score.argMax().getLong().toInt()
    }
}

private fun testModel(
    config: DQNConfig, modelPath: String, createEnv: () -> Environment<Int, FloatArray>
) {
    // test model
    val manager = NDManager.newBaseManager()

    val qNetwork = Model.newInstance("qNetworkPredictor")
    val net = Mlp(config.numInputs, config.numActions, intArrayOf(config.hidden1Size, config.hidden2Size))
    net.initialize(manager, DataType.FLOAT32, Shape(config.numInputs.toLong()))
    qNetwork.block = net

    val bais = DataInputStream(Paths.get(modelPath).toFile().inputStream())
    qNetwork.block.loadParameters(manager, DataInputStream(bais))

    val predictor = qNetwork.newPredictor(NoopTranslator())

    val env = createEnv()
    var totalReward = 0.0f

    // run 100 test episodes
    for (i in 0 until 100) {
        var done = false
        var episodeReward = 0.0f
        var observation = env.reset()
        while (!done) {
            val action = getAction(manager, predictor, observation)
            val step = env.step(action)
            done = step.done
            observation = step.state
            episodeReward += step.reward
        }
        totalReward += episodeReward
    }
    println("average reward: ${totalReward / 100.0f}")

    manager.close()
}
