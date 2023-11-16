use deep_thinker::model::{DQNConfig, Environment, local_dqn_agent, Agent};

mod cartpole;

fn main() {
    let num_inputs = 4;
    let num_actions = 2;

    let config = DQNConfig {
        start_epsilon: 1.0,
        end_epsilon: 0.05,
        exploration_fraction: 0.5,
        learning_starts: 10_000,
        train_frequency: 10,
        batch_size: 128,
        learning_rate: 0.00025,
        target_network_frequency: 500,
        gamma: 0.99,
        num_inputs,
        num_actions,
        hidden_1_size: 120,
        hidden_2_size: 84,
        total_timesteps: 500_000,
    };

    let mut env = cartpole::Cartpole::new();
    let agent = local_dqn_agent(config);

    let total_episodes = 5000;
    for episode in 0..total_episodes {
        let mut obs = env.reset();
        let mut episode_reward = 0.0;
        let mut action = agent.get_first_action(obs.clone());
        loop {
            let (next_obs, reward, done) = env.step(action);
            episode_reward += reward;
            obs = next_obs;

            action = agent.get_action(obs.clone(), reward, done);

            if done {
                break;
            }
        }
        println!("Episode {}, reward {}", episode, episode_reward);
    }
}
