package org.deep_thinker.cartpole_dqn;

import org.deep_thinker.agent.dqn.DQNConfig;
import org.deep_thinker.env.cartpole.Cartpole;
import org.deep_thinker.model.Step;

public class CartpoleDqnZeroMQMain {
    public static void main(String[] args) {
        var config = new DQNConfig(
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

        var totalEpisodes = 5000;

        Cartpole cartpole = new Cartpole();
        for (int i = 0; i < totalEpisodes; i++) {
            float episodeReward = 0.0f;
            float[] state = cartpole.reset();

            // get first action
            int action = 0;

            do {
                Step step = cartpole.step(action);
                episodeReward += step.getReward();

                if (step.getDone()) {
                    // send episode complete

                    //bus.send(episodeCompleteTopic, EpisodeComplete(envId, step.state, step.reward, episodeReward))
                    break;
                } else {
                    action = 0;
                    // get action
                    //bus.send(getActionTopic, GetAction(envId, step.state, step.reward))
                }
            } while (true);

            System.out.println("Episode " + i);
        }
    }
}
