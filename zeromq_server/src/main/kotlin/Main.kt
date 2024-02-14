package org.example

import io.vertx.core.Vertx
import org.example.org.deep_thinker.zeromq.server.DeepThinkerZeroMQServer

fun main() {
    val server = DeepThinkerZeroMQServer(Vertx.vertx())
    server.start()
}