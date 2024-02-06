package org.deep_thinker.zeromq.client;

import org.deep_thinker.model.DQNConfig;
import org.deep_thinker.model.DeepThinkerClient;
import org.jetbrains.annotations.NotNull;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ZeroMQClient implements DeepThinkerClient {
    ZMQ.Socket publisher;
    ZMQ.Socket subscriber;
    ZContext context;
    String clientId = UUID.randomUUID().toString();
    CompletableFuture<String> future;

    public ZeroMQClient() {
        context = new ZContext();
        publisher = context.createSocket(SocketType.PUB);
        publisher.connect("tcp://localhost:5556");

        subscriber = context.createSocket(SocketType.SUB);
        subscriber.connect("tcp://localhost:5557");
        String topic = "results";
        subscriber.subscribe(topic.getBytes(ZMQ.CHARSET));

        new Thread(() -> {
            while (true) {
                String t = subscriber.recvStr();
                String data = subscriber.recvStr();
                System.out.println("Received: " + data);
                future.complete(data);
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
        publisher.send("to_upper", ZMQ.SNDMORE);
        publisher.send(s);
        future = new CompletableFuture<>();

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
