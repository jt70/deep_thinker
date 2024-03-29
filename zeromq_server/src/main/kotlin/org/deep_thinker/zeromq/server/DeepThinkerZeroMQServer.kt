package org.example.org.deep_thinker.zeromq.server

import com.google.flatbuffers.FlatBufferBuilder
import io.vertx.core.Vertx
import org.deep_thinker.model.MessageTypes
import org.deep_thinker.model.RequestMetadata
import org.deep_thinker.model.ResponseMetadata
import org.deep_thinker.serde.IntFlatSerde
import org.deep_thinker.serde.RequestMetadataSerde
import org.deep_thinker.serde.ResponseMetadataSerde
import org.deep_thinker.serde.StringFlatSerde
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ


class DeepThinkerZeroMQServer(vertx: Vertx) {
    val intFlatSerde = IntFlatSerde()
    val requestMetadataSerde = RequestMetadataSerde()
    val responseMetadataSerde = ResponseMetadataSerde()
    val responseMessageTypeBytes: ByteArray = intFlatSerde.serialize(MessageTypes.RESPONSE)
    var eventBus = vertx.eventBus()
    lateinit var publisher: ZMQ.Socket

    fun start() {
        val context = ZContext()

        val subscriber = context.createSocket(SocketType.SUB)
        subscriber.bind("tcp://*:5556")
        subscriber.subscribe(ZMQ.SUBSCRIPTION_ALL)

        publisher = context.createSocket(SocketType.PUB)
        publisher.bind("tcp://*:5557")

        val stringSerde = StringFlatSerde()
        eventBus.consumer("toUpperCase") { message ->
            val input = stringSerde.deserialize(message.body())
            val response = stringSerde.serialize(input.uppercase())
            message.reply(response)
        }

        Thread {
            println("Start receiving message")
            while (true) {
                val topic = subscriber.recvStr()
                val messageTypeBytes = subscriber.recv()
                val messageType = getMessageType(messageTypeBytes)

                if (messageType == MessageTypes.REQUEST) {
                    val requestMetaData: ByteArray = subscriber.recv()
                    val requestContent: ByteArray = subscriber.recv()
                    handleRequestResponse(topic, requestMetaData, requestContent)
                } else {
                    throw RuntimeException("Unknown message type!")
                }
            }
        }.start()
        // TODO: fix this
        Thread.sleep(200)
    }

    private fun handleRequestResponse(topic: String, requestMetaDataBytes: ByteArray, requestContent: ByteArray) {
        val requestMetaData = requestMetadataSerde.deserialize(requestMetaDataBytes)
        val responseTopic = requestMetaData.responseTopic()
        val responseId = requestMetaData.responseId()

        eventBus.request<ByteArray>(topic, requestContent) { message ->
            val builder = FlatBufferBuilder(20)
            val responseIdOffset = builder.createString(responseId)
            val offset = ResponseMetadata.createResponseMetadata(builder, responseIdOffset)
            builder.finish(offset)
            val responseMetadata = builder.sizedByteArray()

            synchronized(publisher) {
                publisher.send(responseTopic, ZMQ.SNDMORE)
                publisher.send(responseMessageTypeBytes, ZMQ.SNDMORE)
                publisher.send(responseMetadata, ZMQ.SNDMORE)
                publisher.send(message.result().body())
            }
        }
    }

    private fun getMessageType(data: ByteArray): Int {
        return intFlatSerde.deserialize(data)
    }
}
