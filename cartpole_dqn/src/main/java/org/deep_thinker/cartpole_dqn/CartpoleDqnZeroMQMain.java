package org.deep_thinker.cartpole_dqn;

import org.deep_thinker.model.DQNConfig;
import org.deep_thinker.env.cartpole.Cartpole;
import org.deep_thinker.model.DeepThinkerClient;
import org.deep_thinker.model.Step;
import org.deep_thinker.zeromq.client.ZeroMQClient;
import org.deep_thinker.zeromq.client.ZeroMQFlatBufferClient;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CartpoleDqnZeroMQMain {
    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
//        DeepThinkerClient client = new ZeroMQFlatBufferClient();
//        String agentId = "cartpole_dqn";
//        var config = new DQNConfig(
//                agentId,
//                1.0f,
//                0.05f,
//                0.5f,
//                10_000,
//                10,
//                128,
//                0.00025f,
//                500,
//                0.99f,
//                4,
//                2,
//                120,
//                84,
//                500_000,
//                10_000
//        );
//
//        Integer totalEpisodes = 5000;
//        client.createDQNAgent(config).get(5, TimeUnit.SECONDS);
//
//        String envId = "my_env";
//
//        Cartpole cartpole = new Cartpole();
//        for (int i = 0; i < totalEpisodes; i++) {
//            float episodeReward = 0.0f;
//            float[] state = cartpole.reset();
//            Integer action = client.getFirstAction(agentId, envId, state).get();
//            Step step;
//
//            do {
//                step = cartpole.step(action);
//                episodeReward += step.getReward();
//
//                if (!step.getDone()) {
//                    action = client.getAction(agentId, envId, step.getState(), step.getReward()).get();
//                }
//            } while (!step.getDone());
//
//            client.episodeComplete(agentId, envId, step.getState(), step.getReward(), episodeReward).get();
//
//            if (i % 100 == 0) {
//                System.out.println("Episode " + i + " reward: " + episodeReward);
//            }
//        }
    }
}
