package org.deep_thinker.cartpole_dqn;

import io.vertx.core.Vertx;
import org.deep_thinker.env.cartpole.Cartpole;
import org.deep_thinker.model.DQNConfig;
import org.deep_thinker.model.DeepThinkerClient;
import org.deep_thinker.model.Step;
import org.deep_thinker.verticles.DQNAgentFactoryVerticle;
import org.deep_thinker.zeromq.client.ZeroMQClient;
import org.example.org.deep_thinker.zeromq.server.DeepThinkerZeroMQServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CartpoleDQNTest {

    @BeforeAll
    public static void setUp() {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new DQNAgentFactoryVerticle());

        var server = new DeepThinkerZeroMQServer(vertx);
        server.start();
    }

    // Message Pack Total time: 46932 ms
    @Test
    public void testCartpoleDQN() throws ExecutionException, InterruptedException, TimeoutException {
        DeepThinkerClient client = new ZeroMQClient();

        String agentId = "cartpole_dqn";
        var config = new DQNConfig(
                agentId,
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

        //Integer totalEpisodes = 5000;
        Integer totalEpisodes = 20000;
        client.createDQNAgent(config).get(50, TimeUnit.SECONDS);
        Thread.sleep(500);

        String envId = "my_env";

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
}
