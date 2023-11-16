use std::marker::PhantomData;
use burn::backend::WgpuBackend;
use burn::module::{ADModule, Module};
use burn::nn;
use burn::optim::{AdamConfig, Optimizer};
use burn::tensor::backend::{ADBackend, Backend};
use burn::tensor::Tensor;
use burn_autodiff::ADBackendDecorator;
use burn_wgpu::AutoGraphicsApi;
use deep_thinker::agents::dqn::Agent;
//use deep_thinker::agents::dqn::{DQNAgent, DQNConfig};
use deep_thinker::model::Environment;

mod cartpole;

type MyBackend = WgpuBackend<AutoGraphicsApi, f32, i32>;
type MyAutodiffBackend = ADBackendDecorator<MyBackend>;

fn main() {
    let agent = Agent::<MyAutodiffBackend>::new();
    println!("{}", agent.step());
    // let optimizer = AdamConfig::new();
    // let optim = optimizer.init::<MyAutodiffBackend, QNetwork<MyAutodiffBackend>>();
    //
    // let fc1 = nn::LinearConfig::new(2, 2)
    //     .with_bias(true)
    //     .init();
    //
    // let model = QNetwork::<MyAutodiffBackend> {
    //     fc1,
    // };
    //
    // let ones = Tensor::<MyAutodiffBackend, 2>::full([10, 1], 1.0);
    //
    // let agent = Agent {
    //     optim,
    //     model,
    //     ones,
    // };
    //
    // agent.step();
    //
}

// fn main_old() {
//     // TODO: get input/output size from environment
//     let num_inputs = 4;
//     let num_actions = 2;
//
//     let config = DQNConfig {
//         start_epsilon: 1.0,
//         end_epsilon: 0.05,
//         exploration_fraction: 0.5,
//         learning_starts: 10_000,
//         train_frequency: 10,
//         batch_size: 128,
//         learning_rate: 0.00025,
//         target_network_frequency: 500,
//         gamma: 0.99,
//         num_inputs,
//         num_actions,
//         hidden_1_size: 120,
//         hidden_2_size: 84,
//         total_timesteps: 500_000,
//     };
//     let mut agent = DQNAgent::new(config);
//     let mut env = cartpole::Cartpole::new();
//
//     let total_episodes = 5000;
//     for episode in 0..total_episodes {
//         let mut obs = env.reset();
//         let mut episode_reward = 0.0;
//         let mut action = agent.get_first_action(obs.clone());
//         loop {
//             let (next_obs, reward, done) = env.step(action);
//             episode_reward += reward;
//             obs = next_obs;
//
//             if done {
//                 agent.end_episode(obs.clone(), reward);
//                 break;
//             } else {
//                 action = agent.get_action(obs.clone(), reward);
//             }
//         }
//         println!("Episode {}, reward {}", episode, episode_reward);
//     }
// }
