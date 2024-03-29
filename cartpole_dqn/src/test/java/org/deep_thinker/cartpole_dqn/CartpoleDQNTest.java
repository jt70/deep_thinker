package org.deep_thinker.cartpole_dqn;

import com.google.flatbuffers.FlatBufferBuilder;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
//import org.deep_thinker.agent.dqn.djl.DQNAgentFactoryVerticle;
import org.deep_thinker.agent.dqn.deep_thinker_dl.DQNAgentFactoryVerticle;
import org.deep_thinker.env.cartpole.Cartpole;
import org.deep_thinker.model.DQNConfigFlat;
import org.deep_thinker.model.DeepThinkerClient;
import org.deep_thinker.model.Step;
import org.deep_thinker.zeromq.client.ZeroMQFlatBufferClient;
import org.example.org.deep_thinker.zeromq.server.DeepThinkerZeroMQServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CartpoleDQNTest {

    @BeforeAll
    public static void setUp() {
        Vertx vertx = Vertx.vertx(new VertxOptions().setBlockedThreadCheckInterval(1000 * 60 * 60));

        vertx.deployVerticle(new DQNAgentFactoryVerticle());

        var server = new DeepThinkerZeroMQServer(vertx);
        server.start();
    }

    @Test
    public void testCartpoleDQN() throws ExecutionException, InterruptedException, TimeoutException {
        runCartpole(10000);
    }

    private void runCartpole(int totalEpisodes) throws ExecutionException, InterruptedException, TimeoutException {
        DeepThinkerClient client = new ZeroMQFlatBufferClient();

        String agentId = "cartpole_dqn";

        FlatBufferBuilder builder = new FlatBufferBuilder(20);
        int agentIdOffset = builder.createString(agentId);
        int offset = DQNConfigFlat.createDQNConfigFlat(
                builder,
                agentIdOffset,
                1.0f,
                0.05f,
                0.5f,
                10_000,
                10,
                128,
                0.00025f,
                500,
                0.99f,
                4,
                2,
                120,
                84,
                500_000,
                10_000
        );
        builder.finish(offset);
        ByteBuffer buffer = ByteBuffer.wrap(builder.sizedByteArray());
        var config = DQNConfigFlat.getRootAsDQNConfigFlat(buffer);

        client.createDQNAgent(config).get(50, TimeUnit.SECONDS);
        Thread.sleep(500); // TODO: probably don't need this

        String envId = UUID.randomUUID().toString();

        float totalReward = 0.0f;
        long start = System.currentTimeMillis();
        Cartpole cartpole = new Cartpole();
        for (int i = 0; i < totalEpisodes; i++) {
            float episodeReward = 0.0f;
            float[] state = cartpole.reset();
            Integer action = client.getFirstAction(agentId, envId, state).get();
            Step step;

            do {
                step = cartpole.step(action);
                episodeReward += step.getReward();

                if (!step.getDone()) {
                    action = client.getAction(agentId, envId, step.getState(), step.getReward()).get();
                }
            } while (!step.getDone());
            totalReward += episodeReward;
            client.episodeComplete(agentId, envId, step.getState(), step.getReward(), episodeReward).get();

            if (i % 100 == 0) {
                System.out.println("Episode " + i + " reward: " + episodeReward);
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("Total reward: " + totalReward);
        System.out.println("Total time: " + (end - start) + " ms");
    }

    @Test
    public void multiThreadedTest() throws InterruptedException {
        int numThreads = 10;
        int episodes = 1000;
        List<Thread> threads = new java.util.ArrayList<>(numThreads);
        for (int i=0; i<numThreads; i++) {
            Thread t = new Thread(() -> {
                try {
                    runCartpole(episodes);
                } catch (ExecutionException | InterruptedException | TimeoutException e) {
                    throw new RuntimeException(e);
                }
            });
            threads.add(t);
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }
    }

    @Test
    public void testToUpperCase() throws ExecutionException, InterruptedException, TimeoutException {
        DeepThinkerClient zeroMQClient = new ZeroMQFlatBufferClient();
        String upper = zeroMQClient.toUpperCase("hello").get(5, TimeUnit.SECONDS);
        assertEquals("HELLO", upper);
    }

    @Test
    public void testMultiThreaded() throws InterruptedException {
        new Thread(() -> {
            try {
                DeepThinkerClient zeroMQClient = new ZeroMQFlatBufferClient();
                while (true) {
                    String upper = zeroMQClient.toUpperCase("hello 1").get(5, TimeUnit.SECONDS);
                    assertEquals("HELLO 1", upper);
                }
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> {
            try {
                DeepThinkerClient zeroMQClient = new ZeroMQFlatBufferClient();
                while (true) {
                    String upper = zeroMQClient.toUpperCase("hello 2").get(5, TimeUnit.SECONDS);
                    assertEquals("HELLO 2", upper);
                }
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                throw new RuntimeException(e);
            }
        }).start();

        Thread.sleep(10000);
    }
}
