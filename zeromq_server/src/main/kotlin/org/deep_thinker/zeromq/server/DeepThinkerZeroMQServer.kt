package org.example.org.deep_thinker.zeromq.server

import io.vertx.core.Vertx
import org.msgpack.core.MessagePack
import org.msgpack.core.MessageUnpacker
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

        eventBus.consumer<ByteArray>("to_upper") { message ->
            val unpacker: MessageUnpacker = MessagePack.newDefaultUnpacker(message.body())
            val responseTopic = unpacker.unpackString()
            val responseId = unpacker.unpackString()
            val input = unpacker.unpackString()

            val response = MessagePack.newDefaultBufferPacker()
            response
                .packString(responseId)
                .packString(input.uppercase())
            response.close()

            publisher.send(responseTopic, ZMQ.SNDMORE)
            publisher.send(response.toByteArray())
        }

        println("Start receiving message")
        while (true) {
            val topic = subscriber.recvStr()
            val data = subscriber.recv()
            eventBus.send(topic, data)
        }
    }
}