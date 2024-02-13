package org.deep_thinker.cartpole_dqn;

import com.google.flatbuffers.FlatBufferBuilder;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.deep_thinker.model.DQNConfig;
import org.deep_thinker.env.cartpole.Cartpole;
import org.deep_thinker.model.DQNConfigFlat;
import org.deep_thinker.model.DeepThinkerClient;
import org.deep_thinker.model.Step;
import org.deep_thinker.verticles.DQNAgentFactoryVerticle;
import org.deep_thinker.zeromq.client.ZeroMQClient;
import org.deep_thinker.zeromq.client.ZeroMQFlatBufferClient;
import org.example.org.deep_thinker.zeromq.server.DeepThinkerZeroMQServer;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CartpoleDqnZeroMQMain {
    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        Vertx vertx = Vertx.vertx(new VertxOptions().setBlockedThreadCheckInterval(1000 * 60 * 60));

        vertx.deployVerticle(new DQNAgentFactoryVerticle());

        var server = new DeepThinkerZeroMQServer(vertx);
        server.start();
        Thread.sleep(1000);

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

        Integer totalEpisodes = 5000;
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
