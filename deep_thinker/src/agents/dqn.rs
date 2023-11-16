use burn::tensor::backend::{ADBackend, Backend};
use burn::backend::WgpuBackend;
use burn::{LearningRate, nn};
use burn::module::{ADModule, Module};
use burn::nn::loss::CrossEntropyLoss;
use burn::optim::{AdamConfig, GradientsParams, Optimizer};
use burn::tensor::{Data, Int, Shape, Tensor};

#[derive(Module, Debug)]
struct QNetwork<B: Backend> {
    fc1: nn::Linear<B>,
}

pub struct Agent<B: Backend> {
    model: QNetwork<B>,
    tensor: Tensor<B, 2>,
    pub optimize: fn(Tensor<B, 2>, Tensor<B, 1, Int>, QNetwork<B>, LearningRate) -> u32,
}

impl<B> Agent<B>
    where
        B: ADBackend,
{
    pub fn new() -> Self {
        let optimizer = AdamConfig::new();
        let mut optim = optimizer.init::<B, QNetwork<B>>();

        let optimize = |logits: Tensor<B, 2>, targets: Tensor<B, 1, Int>, model: QNetwork<B>, lr: LearningRate| -> u32 {
            let loss = CrossEntropyLoss::new(None).forward(logits, targets);
            let grads = loss.backward();
            let grads = GradientsParams::from_grads(grads, &model);
            let new_model = optim.step(lr, model, grads);
            1
        };

        let fc1 = nn::LinearConfig::new(2, 2)
            .with_bias(true)
            .init();

        let model = QNetwork::<B> {
            fc1,
        };

        let tensor = Tensor::<B, 2>::full([10, 1], 1.0);

        let agent = Agent {
            model,
            tensor,
            optimize,
        };

        agent
    }

    pub fn step(&self) -> u32 {
        //(self.add_one_v2)(1)
        0
    }
}

//
//
// impl<M, O, B> DQNAgent<M, O, B>
//     where
//         B: ADBackend,
//         M: ADModule<B>,
//         O: Optimizer<M, B>,
// {
//     pub fn new(config: DQNConfig) -> Self {
//         //    let gamma = Tensor::<MyAutodiffBackend, 2>::full([batch_size, 1], 0.99);
//         let ones = Tensor::<MyAutodiffBackend, 2>::full([config.batch_size, 1], 1.0);
//
//         let random_action_sampler = Uniform::from(0..config.num_actions as i32);
//         let rand = rand::thread_rng();
//         let mut q_network = QNetwork::<MyAutodiffBackend>::new(4, 120, 84, config.num_actions);
//
//         let optimizer = AdamConfig::new();
//         let optim = optimizer.init::<MyAutodiffBackend, QNetwork<MyAutodiffBackend>>();
//
// //        let mut optim: dyn Optimizer<MyAutodiffBackend, QNetwork<MyAutodiffBackend>> = optimizer.init();
//         let mut target_network = q_network.clone();
//
//         let mut rb = ReplayBuffer::new(10_000);
//
//         DQNAgent {
//             // learning_rate: LearningRate::from(config.learning_rate),
//             // random_action_sampler,
//             // rand,
//             // start_epsilon: config.start_epsilon,
//             // end_epsilon: config.end_epsilon,
//             // exploration_fraction: config.exploration_fraction,
//             // total_timesteps: config.total_timesteps,
//             // global_step: 0,
//             // rb,
//             q_network,
//             target_network,
//             optim,
//             ones
//         }
//     }
//
//     pub fn get_first_action(&self, obs: Vec<f32>) -> i32 {
//         //self.random_action_sampler.sample(&mut rand::thread_rng())
//         0
//     }
//
//     pub fn end_episode(&self, obs: Vec<f32>, reward: f32) {
//     }
//
//     pub fn get_action(&mut self, obs: Vec<f32>, reward: f32) -> i32 {
//         0
//         // let epsilon = linear_schedule(self.start_epsilon, self.end_epsilon, self.exploration_fraction * self.total_timesteps as f32, self.global_step);
//         //
//         // let action = if self.rand.gen::<f32>() < epsilon {
//         //     self.random_action_sampler.sample(&mut self.rand)
//         // } else {
//         //     let data = Data::new(obs.clone(), Shape::new([1, obs.len()]));
//         //     let input = Tensor::<MyAutodiffBackend, 2>::from_data(data);
//         //     let action = self.q_network.forward(input).argmax(1).into_scalar();
//         //     action
//         // };
//         //
//         // action
//     }
//
//
// }

// fn linear_schedule(start_e: f32, end_e: f32, duration: f32, t: u64) -> f32 {
//     let slope = (end_e - start_e) / duration;
//     end_e.max(slope * t as f32 + start_e)
// }
//
// // TODO: probably a better way to do this
// fn vecs_to_tensor(input: Vec<Vec<f32>>) -> Tensor<MyAutodiffBackend, 2> {
//     let input_vectors = input.iter().map(|v|
//         {
//             let data = Data::new(v.clone(), Shape::new([1, v.len()]));
//             Tensor::<MyAutodiffBackend, 2>::from_data(data)
//         }).collect::<Vec<Tensor<MyAutodiffBackend, 2>>>();
//     let t = Tensor::cat(input_vectors, 0);
//     t
// }

/*

    let mut q_network = QNetwork::<MyAutodiffBackend>::new(4, 120, 84, num_actions);
    //let mut q_network = QNetwork::<MyAutodiffBackend>::new(4, 10, 10, num_actions);
    let optimizer = AdamConfig::new();
    let mut optim = optimizer.init();
    let mut target_network = q_network.clone();

    let mut rb = ReplayBuffer::new(10_000);

    let mut obs = env.reset();
    let mut episode_reward = 0.0;
    let mut episode_number = 0;

    for global_step in 0..total_timesteps {
        let epsilon = linear_schedule(start_e, end_e, exploration_fraction * total_timesteps as f32, global_step);

        let action = if rng.gen::<f32>() < epsilon {
            random_action_sampler.sample(&mut rng)
        } else {
            let data = Data::new(obs.clone(), Shape::new([1, obs.len()]));
            let input = Tensor::<MyAutodiffBackend, 2>::from_data(data);
            let action = q_network.forward(input).argmax(1).into_scalar();
            action
        };

        let (next_obs, reward, done) = env.step(action);
        rb.add(obs.clone(), action, reward, next_obs.clone(), done);
        episode_reward += reward;

        obs = if done {
            println!("Episode {}, reward {}, global_step {}", episode_number, episode_reward, global_step);
            episode_reward = 0.0;
            episode_number += 1;
            env.reset()
        } else {
            next_obs
        };

        if global_step > learning_starts {
            if global_step % train_frequency == 0 {
                let (obs, actions, rewards, next_obs, dones) = rb.sample(batch_size);

                let next_obs_tensor = vecs_to_tensor(next_obs);
                let target = target_network.forward(next_obs_tensor);
                let target_max = target.clone().max_dim(1);

                let rewards_data = Data::new(rewards.clone(), Shape::new([rewards.len(), 1]));
                let rewards_tensor = Tensor::<MyAutodiffBackend, 2>::from_data(rewards_data);

                let dones_data = Data::new(dones.clone(), Shape::new([dones.len(), 1]));
                let dones_tensor = Tensor::<MyAutodiffBackend, 2>::from_data(dones_data);

                let td_target = rewards_tensor + gamma.clone() * target_max * (ones.clone() - dones_tensor);

                let obs_tensor = vecs_to_tensor(obs);
                let q_values = q_network.forward(obs_tensor);

                let actions_data = Data::new(actions.clone(), Shape::new([actions.len(), 1]));
                let actions_tensor = Tensor::<MyAutodiffBackend, 2, Int>::from_data(actions_data);
                let old_val = q_values.gather(1, actions_tensor);

                let loss = MSELoss::new().forward(old_val.clone(), td_target, Reduction::Mean);

                let gradients = loss.backward();
                let gradient_params = GradientsParams::from_grads(gradients, &q_network);

                q_network = optim.step(learning_rate, q_network, gradient_params);
            }

            if global_step % target_network_frequency == 0 {
                target_network = q_network.clone();
            }
        }
    }

 */