package org.deep_thinker.zeromq.client;

import com.google.flatbuffers.FlatBufferBuilder;
import org.deep_thinker.model.*;
import org.deep_thinker.serde.*;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static org.deep_thinker.model.MessageTypes.REQUEST;
import static org.deep_thinker.model.MessageTypes.RESPONSE;

public class ZeroMQFlatBufferClient implements DeepThinkerClient {
    ZMQ.Socket publisher;
    ZMQ.Socket subscriber;
    ZContext context;
    String responseTopic;
    Map<String, Consumer<byte[]>> responseHandlers;
    StringFlatSerde stringSerde = new StringFlatSerde();
    IntFlatSerde intSerde = new IntFlatSerde();
    DQNConfigFlatSerde dqnConfigSerde = new DQNConfigFlatSerde();
    GetFirstActionFlat getFirstActionSerde = new GetFirstActionFlat();
    GetActionFlat getActionSerde = new GetActionFlat();
    EpisodeCompleteFlatSerde episodeCompleteSerde = new EpisodeCompleteFlatSerde();
    byte[] requestMessageTypeBytes;

    public ZeroMQFlatBufferClient() {
        requestMessageTypeBytes = intSerde.serialize(REQUEST);

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

    private void handleResponse(byte[] responseMetaData, byte[] messageContent) {
        String responseId = stringSerde.deserialize(responseMetaData);
        Consumer<byte[]> handler = responseHandlers.get(responseId);
        if (handler != null) {
            handler.accept(messageContent);
        }
        responseHandlers.remove(responseId);
    }

    private int getMessageType(byte[] bytes) {
        return intSerde.deserialize(bytes);
    }

    @Override
    public CompletableFuture<String> toUpperCase(String s) {
        return requestResponse("toUpperCase", stringSerde.serialize(s), stringSerde);
    }

    public <O> CompletableFuture<O> requestResponse(String topic, byte[] requestContent, FlatSerde<O> deserializer) {
        String responseId = UUID.randomUUID().toString();

        FlatBufferBuilder builder = new FlatBufferBuilder(20);
        int responseTopicOffset = builder.createString(responseTopic);
        int responseIdOffset = builder.createString(responseId);
        int offset = RequestMetadata.createRequestMetadata(builder, responseTopicOffset, responseIdOffset);
        builder.finish(offset);
        byte[] requestMetadata = builder.sizedByteArray();

        CompletableFuture<O> future = new CompletableFuture<>();
        Consumer<byte[]> consumer = (byte[] bytes) -> {
            O response = deserializer.deserialize(bytes);
            future.complete(response);
        };
        responseHandlers.put(responseId, consumer);

        publisher.send(topic, ZMQ.SNDMORE);
        publisher.send(requestMessageTypeBytes, ZMQ.SNDMORE);
        publisher.send(requestMetadata);
        publisher.send(requestContent);

        return future;
    }

    @Override
    public CompletableFuture<Integer> createDQNAgent(DQNConfigFlat config) {
        return requestResponse("createDQNAgent", dqnConfigSerde.serialize(config), intSerde);
    }

    @Override
    public CompletableFuture<Integer> getFirstAction(String agentId, String envId, float[] state) {
        FlatBufferBuilder builder = new FlatBufferBuilder(20);
        int envIdOffset = builder.createString(envId);
        int stateVectorOffset = GetFirstActionFlat.createStateVector(builder, state);
        int offset = GetFirstActionFlat.createGetFirstActionFlat(builder, envIdOffset, stateVectorOffset);
        builder.finish(offset);
        byte[] bytes = builder.sizedByteArray();

        return requestResponse("getFirstAction." + agentId, bytes, intSerde);
    }

    @Override
    public CompletableFuture<Integer> getAction(String agentId, String envId, float[] state, float reward) {
        FlatBufferBuilder builder = new FlatBufferBuilder(20);
        int envIdOffset = builder.createString(envId);
        int stateVectorOffset = GetActionFlat.createStateVector(builder, state);
        int offset = GetActionFlat.createGetActionFlat(builder, envIdOffset, reward, stateVectorOffset);
        builder.finish(offset);
        byte[] bytes = builder.sizedByteArray();

        return requestResponse("getAction." + agentId, bytes, intSerde);
    }

    @Override
    public CompletableFuture<Integer> episodeComplete(String agentId, String envId, float[] state, float reward, float episodeReward) {
        FlatBufferBuilder builder = new FlatBufferBuilder(20);
        int envIdOffset = builder.createString(envId);
        int stateVectorOffset = EpisodeCompleteFlat.createStateVector(builder, state);
        int offset = EpisodeCompleteFlat.createEpisodeCompleteFlat(builder, envIdOffset, stateVectorOffset, reward, episodeReward);
        builder.finish(offset);
        byte[] bytes = builder.sizedByteArray();

        return requestResponse("episodeComplete." + agentId, bytes, intSerde);
    }
}
