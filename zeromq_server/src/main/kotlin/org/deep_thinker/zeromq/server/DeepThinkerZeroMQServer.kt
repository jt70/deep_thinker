package org.example.org.deep_thinker.zeromq.server

import io.vertx.core.Vertx
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ

class DeepThinkerZeroMQServer {
    fun start() {
        val context = ZContext()
        val vertx = Vertx.vertx()
        val eventBus = vertx.eventBus()

        val subscriber = context.createSocket(SocketType.SUB)
        subscriber.bind("tcp://*:5556")
        subscriber.subscribe(ZMQ.SUBSCRIPTION_ALL)

        val publisher = context.createSocket(SocketType.PUB)
        publisher.bind("tcp://*:5557")

        eventBus.consumer<String>("to_upper") { message ->
            println("Received message: ${message.body()}")

            publisher.send("results", ZMQ.SNDMORE)
            publisher.send(message.body().uppercase())
        }

        println("Start receiving message")
        while (true) {
            val topic = subscriber.recvStr()
            val data = subscriber.recvStr()

            eventBus.send(topic, data)
        }
    }
}