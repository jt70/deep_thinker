package org.deep_thinker.zeromq.client;

import org.deep_thinker.model.DQNConfig;
import org.deep_thinker.model.DeepThinkerClient;
import org.deep_thinker.serde.*;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static org.deep_thinker.model.MessageTypes.REQUEST;
import static org.deep_thinker.model.MessageTypes.RESPONSE;

public class ZeroMQClient implements DeepThinkerClient {
    ZMQ.Socket publisher;
    ZMQ.Socket subscriber;
    ZContext context;
    String responseTopic;
    Map<String, Consumer<byte[]>> responseHandlers;
    DQNConfigSerde dqnConfigSerde = new DQNConfigSerde();
    StringMessagePackSerde stringSerde = new StringMessagePackSerde();
    VoidSerde voidSerde = new VoidSerde();
    IntMessagePackSerde intSerde = new IntMessagePackSerde();
    byte[] requestMessageTypeBytes;

    public ZeroMQClient() {
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        try {
            packer.packInt(REQUEST);
            packer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        requestMessageTypeBytes = packer.toByteArray();

        responseTopic = UUID.randomUUID().toString();
        responseHandlers = new ConcurrentHashMap<>();
        context = new ZContext();
        publisher = context.createSocket(SocketType.PUB);
        publisher.connect("tcp://localhost:5556");

        subscriber = context.createSocket(SocketType.SUB);
        subscriber.connect("tcp://localhost:5557");
        subscriber.subscribe(responseTopic.getBytes(ZMQ.CHARSET));

        new Thread(() -> {
            while (true) {
                subscriber.recvStr(); // topic
                int messageType = getMessageType(subscriber.recv());

                if (messageType == RESPONSE) {
                    byte[] responseMetaData = subscriber.recv();
                    byte[] messageContent = subscriber.recv();
                    handleResponse(responseMetaData, messageContent);
                } else {
                    throw new RuntimeException("Unexpected message type: " + messageType);
                }
            }
        }).start();

        try {
            // TODO: fix
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<String> toUpperCase(String s) {
        return requestResponse("toUpperCase", s, stringSerde, stringSerde);
    }

    @Override
    public CompletableFuture<Void> createDQNAgent(DQNConfig config) {
        return requestResponse("createDQNAgent", config, dqnConfigSerde, voidSerde);
    }

    @Override
    public CompletableFuture<Integer> getFirstAction(String agentId, String envId, float[] state) {
        return requestResponse("getFirstAction." + agentId, "yo", stringSerde, intSerde);
    }

    @Override
    public CompletableFuture<Integer> getAction(String agentId, String envId, float[] state, float reward) {
        return requestResponse("getAction." + agentId, "yo", stringSerde, intSerde);
    }

    @Override
    public CompletableFuture<Void> episodeComplete(String agentId, String envId, float[] state, float reward, float episodeReward) {
        return requestResponse("episodeComplete." + agentId, "yo", stringSerde, voidSerde);
    }

    public <I, O> CompletableFuture<O> requestResponse(String topic, I value, MessagePackSerde<I> serializer, MessagePackSerde<O> deserializer) {
        String responseId = UUID.randomUUID().toString();

        MessageBufferPacker requestMetadata = MessagePack.newDefaultBufferPacker();
        try {
            requestMetadata
                    .packString(responseTopic)
                    .packString(responseId);
            requestMetadata.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        MessageBufferPacker requestContent = MessagePack.newDefaultBufferPacker();
        try {
            serializer.serialize(requestContent, value);
            requestContent.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        CompletableFuture<O> future = new CompletableFuture<>();

        Consumer<byte[]> consumer = (byte[] bytes) -> {
            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(bytes);
            O response = deserializer.deserialize(unpacker);
            future.complete(response);
        };
        responseHandlers.put(responseId, consumer);

        publisher.send(topic, ZMQ.SNDMORE);
        publisher.send(requestMessageTypeBytes, ZMQ.SNDMORE);
        publisher.send(requestMetadata.toByteArray());
        publisher.send(requestContent.toByteArray());

        return future;
    }

    private void handleResponse(byte[] responseMetaData, byte[] messageContent) {
        try {
            MessageUnpacker metadataUnpacker = MessagePack.newDefaultUnpacker(responseMetaData);
            String responseId = metadataUnpacker.unpackString();
            Consumer<byte[]> handler = responseHandlers.get(responseId);
            if (handler != null) {
                handler.accept(messageContent);
            }
            responseHandlers.remove(responseId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int getMessageType(byte[] data) {
        MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(data);
        try {
            return unpacker.unpackInt();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
