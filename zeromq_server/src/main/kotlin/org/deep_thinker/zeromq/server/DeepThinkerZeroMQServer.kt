package org.example.org.deep_thinker.zeromq.server

import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import org.deep_thinker.model.MessageTypes
import org.msgpack.core.MessagePack
import org.msgpack.core.MessageUnpacker
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import java.io.IOException


class DeepThinkerZeroMQServer {
    lateinit var eventBus: EventBus
    lateinit var publisher: ZMQ.Socket
    lateinit var responseMessageTypeBytes: ByteArray

    fun start() {
        val packer = MessagePack.newDefaultBufferPacker()
        try {
            packer.packInt(MessageTypes.REQUEST)
            packer.close()
        } catch (e: IOException) {
            throw java.lang.RuntimeException(e)
        }
        responseMessageTypeBytes = packer.toByteArray()

        val context = ZContext()
        val vertx = Vertx.vertx()
        eventBus = vertx.eventBus()

        val subscriber = context.createSocket(SocketType.SUB)
        subscriber.bind("tcp://*:5556")
        subscriber.subscribe(ZMQ.SUBSCRIPTION_ALL)

        publisher = context.createSocket(SocketType.PUB)
        publisher.bind("tcp://*:5557")

        eventBus.consumer<ByteArray>("toUpperCase") { message ->
            val unpacker: MessageUnpacker = MessagePack.newDefaultUnpacker(message.body())
            val input = unpacker.unpackString()

            val response = MessagePack.newDefaultBufferPacker()
            response.packString(input.uppercase())
            response.close()

            message.reply(response.toByteArray())
        }

        println("Start receiving message")
        while (true) {
            val topic = subscriber.recvStr()
            val messageType = getMessageType(subscriber.recv())

            if (messageType == MessageTypes.REQUEST) {
                val requestMetaData: ByteArray = subscriber.recv()
                val requestContent: ByteArray = subscriber.recv()
                handleRequestResponse(topic, requestMetaData, requestContent)
            }
        }
    }

    private fun handleRequestResponse(topic: String, requestMetaData: ByteArray, requestContent: ByteArray) {
        val unpacker: MessageUnpacker = MessagePack.newDefaultUnpacker(requestMetaData)
        val responseTopic = unpacker.unpackString()
        val responseId = unpacker.unpackString()

        eventBus.request<ByteArray>(topic, requestContent) { message ->
            // TODO: make threadsafe
            val responseMetadata = MessagePack.newDefaultBufferPacker()
            responseMetadata.packString(responseId)
            responseMetadata.close()

            publisher.send(responseTopic, ZMQ.SNDMORE)
            publisher.send(responseMessageTypeBytes, ZMQ.SNDMORE)
            publisher.send(responseMetadata.toByteArray(), ZMQ.SNDMORE)
            publisher.send(message.result().body())
        }
    }

    private fun getMessageType(data: ByteArray): Int {
        val unpacker = MessagePack.newDefaultUnpacker(data)
        try {
            return unpacker.unpackInt()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}
