use std::thread;
use crossbeam_channel::Sender;
use deep_thinker::model::{DQNConfig, Environment, local_dqn_agent, Agent, AgentMessage, ChannelAgentProxy};

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

    let agent_sender = local_dqn_agent(config);

    let env_count = 10;
    let mut handles = Vec::with_capacity( env_count);
    for i in 0..env_count {
        println!("Spawning env {}", i);
        let handle = spawn_environment(format!("env{}", i), agent_sender.clone(), 500);
        handles.push(handle);
    }

    for handle in handles {
        handle.join().unwrap();
    }
}

fn spawn_environment(env_id: String, agent_sender: Sender<AgentMessage>, episode_count: i32) -> thread::JoinHandle<()> {
    thread::spawn(move || {
        let mut env = cartpole::Cartpole::new();

        let agent = ChannelAgentProxy::new(env_id.clone(), agent_sender.clone());
        let mut episode_reward = 0.0;
        for episode in 0..episode_count {
            let mut obs = env.reset();
            let mut action = agent.get_first_action(obs.clone());
            loop {
                let (next_obs, reward, done) = env.step(action);
                obs = next_obs;

                episode_reward += reward;
                action = agent.get_action(obs.clone(), reward, done);

                if done {
                    println!("Env id {}, Episode {}, Reward {}", env_id, episode, episode_reward);
                    episode_reward = 0.0;
                    break;
                }
            }
        }
    })
}
