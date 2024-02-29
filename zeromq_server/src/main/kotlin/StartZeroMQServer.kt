package org.example

import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import org.deep_thinker.agent.dqn.deep_thinker_dl.DQNAgentFactoryVerticle
import org.example.org.deep_thinker.zeromq.server.DeepThinkerZeroMQServer

fun main() {
    val vertx = Vertx.vertx(VertxOptions().setBlockedThreadCheckInterval(1000 * 60 * 60))
    vertx.deployVerticle(DQNAgentFactoryVerticle())
    val server = DeepThinkerZeroMQServer(vertx)
    server.start()
}