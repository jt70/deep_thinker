package org.deep_thinker.zeromq.client;

import org.deep_thinker.model.DQNConfig;
import org.deep_thinker.model.DeepThinkerClient;
import org.jetbrains.annotations.NotNull;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ZeroMQClient implements DeepThinkerClient {
    ZMQ.Socket publisher;
    ZMQ.Socket subscriber;
    ZContext context;
    String responseTopic;
    Map<String, Consumer<MessageUnpacker>> responseHandlers;

    public ZeroMQClient() {
        responseTopic = UUID.randomUUID().toString();
        responseHandlers = new HashMap<>();
        context = new ZContext();
        publisher = context.createSocket(SocketType.PUB);
        publisher.connect("tcp://localhost:5556");

        subscriber = context.createSocket(SocketType.SUB);
        subscriber.connect("tcp://localhost:5557");
        subscriber.subscribe(responseTopic.getBytes(ZMQ.CHARSET));

        new Thread(() -> {
            while (true) {
                subscriber.recvStr(); // topic
                byte[] data = subscriber.recv();

                MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(data);
                try {
                    String responseId = unpacker.unpackString();
                    Consumer<MessageUnpacker> handler = responseHandlers.get(responseId);
                    if (handler != null) {
                        handler.accept(unpacker);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
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

    @NotNull
    @Override
    public CompletableFuture<String> toUpperCase(@NotNull String s) {
        String responseId = UUID.randomUUID().toString();
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        try {
            packer
                    .packString(responseTopic)
                    .packString(responseId)
                    .packString(s);
            packer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        CompletableFuture<String> future = new CompletableFuture<>();

        Consumer<MessageUnpacker> consumer = (MessageUnpacker unpacker) -> {
            try {
                String response = unpacker.unpackString();
                future.complete(response);
            } catch (IOException e) {
                future.completeExceptionally(e);
            }
        };
        responseHandlers.put(responseId, consumer);

        publisher.send("to_upper", ZMQ.SNDMORE);
        publisher.send(packer.toByteArray());


        return future;
    }

    @NotNull
    @Override
    public CompletableFuture<String> createDQNAgent(@NotNull String agentId, @NotNull DQNConfig config) {
        return CompletableFuture.completedFuture("x");
    }

    @NotNull
    @Override
    public CompletableFuture<Integer> getFirstAction(@NotNull String agentId, @NotNull String envId, @NotNull float[] state) {
        return CompletableFuture.completedFuture(0);
    }

    @NotNull
    @Override
    public CompletableFuture<Integer> getAction(@NotNull String agentId, @NotNull String envId, @NotNull float[] state, float reward) {
        return CompletableFuture.completedFuture(0);
    }

    @NotNull
    @Override
    public CompletableFuture<Void> episodeComplete(@NotNull String agentId, @NotNull String envId, @NotNull float[] state, float reward, float episodeReward) {
        return CompletableFuture.completedFuture(null);
    }
}
