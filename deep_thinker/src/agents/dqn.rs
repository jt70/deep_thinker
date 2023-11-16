use std::collections::HashMap;
use std::thread;
use burn::tensor::backend::{ADBackend, Backend};
use burn::backend::WgpuBackend;
use burn::{LearningRate, nn};
use burn::module::Module;
use burn::nn::loss::{MSELoss, Reduction};
use burn::optim::{AdamConfig, GradientsParams, Optimizer};
use burn::tensor::{Data, Int, Shape, Tensor};
use burn_autodiff::ADBackendDecorator;
use burn_wgpu::AutoGraphicsApi;
use rand::distributions::{Distribution, Uniform};
use rand::Rng;
use crossbeam_channel::{bounded, Sender};
use crate::model::{AgentMessage, DQNConfig, EnvironmentMessage};
use crate::replay_buffer::ReplayBuffer;

type MyBackend = WgpuBackend<AutoGraphicsApi, f32, i32>;
type MyAutodiffBackend = ADBackendDecorator<MyBackend>;

#[derive(Module, Debug)]
struct QNetwork<B: Backend> {
    fc1: nn::Linear<B>,
    fc2: nn::Linear<B>,
    fc3: nn::Linear<B>,
    activation: nn::ReLU,
}

impl<B: ADBackend> QNetwork<B> {
    pub fn new(input_size: usize, fc1_size: usize, fc2_size: usize, output_size: usize) -> Self {
        let fc1 = nn::LinearConfig::new(input_size, fc1_size)
            .with_bias(true)
            .init();
        let fc2 = nn::LinearConfig::new(fc1_size, fc2_size)
            .with_bias(true)
            .init();
        let fc3 = nn::LinearConfig::new(fc2_size, output_size)
            .with_bias(true)
            .init();

        Self {
            fc1,
            fc2,
            fc3,
            activation: nn::ReLU::new(),
        }
    }

    pub fn forward(&self, input: Tensor<B, 2>) -> Tensor<B, 2> {
        let x = self.fc1.forward(input);
        let x = self.activation.forward(x);
        let x = self.fc2.forward(x);
        let x = self.activation.forward(x);
        let actions = self.fc3.forward(x);

        actions
    }
}

fn linear_schedule(start_e: f32, end_e: f32, duration: f32, t: i32) -> f32 {
    let slope = (end_e - start_e) / duration as f32;
    end_e.max(slope * t as f32 + start_e)
}

pub fn spawn_dqn_actor(config: DQNConfig) -> Sender<AgentMessage> {
    let (sender, receiver) = bounded(1000);

    thread::spawn(move || {
        let mut rng = rand::thread_rng();
        let random_action_sampler = Uniform::from(0..config.num_actions as i32);

        let mut q_network = QNetwork::<MyAutodiffBackend>::new(config.num_inputs, config.hidden_1_size, config.hidden_2_size, config.num_actions);
        let optimizer = AdamConfig::new();
        let mut optim = optimizer.init();
        let mut target_network = q_network.clone();

        let gamma = Tensor::<MyAutodiffBackend, 2>::full([config.batch_size, 1], config.gamma);
        let ones = Tensor::<MyAutodiffBackend, 2>::full([config.batch_size, 1], 1.0);

        let mut replay_buffer = ReplayBuffer::new(10_000);

        let mut global_step = 0;
        let learning_rate: LearningRate = config.learning_rate;

        let mut previous_observation = HashMap::<String, (Vec<f32>, i32)>::new();
        let mut env_map = HashMap::<String, Sender<EnvironmentMessage>>::new();

        loop {
            let agent_message = receiver.recv().unwrap();
            match agent_message {
                AgentMessage::LinkEnv { env_id, env_sender } => {
                    env_map.insert(env_id, env_sender.clone());
                    env_sender.send(EnvironmentMessage::LinkAck).unwrap();
                }
                AgentMessage::GetFirstAction { env_id, obs } => {
                    let epsilon = linear_schedule(config.start_epsilon, config.end_epsilon, config.exploration_fraction * config.total_timesteps as f32, global_step);

                    let action = if rng.gen::<f32>() < epsilon {
                        random_action_sampler.sample(&mut rng)
                    } else {
                        let data = Data::new(obs.clone(), Shape::new([1, obs.len()]));
                        let input = Tensor::<MyAutodiffBackend, 2>::from_data(data);
                        let action = q_network.forward(input).argmax(1).into_scalar();
                        action
                    };

                    previous_observation.insert(env_id.clone(), (obs.clone(), action));

                    env_map.get(&env_id).unwrap().send(EnvironmentMessage::Action(action)).unwrap();
                }
                AgentMessage::GetAction { env_id, obs, reward, done } => {
                    // store transition in replay buffer
                    let (prev_obs, prev_action) = previous_observation.get(&env_id).unwrap();
                    replay_buffer.add(prev_obs.clone(), *prev_action, reward, obs.clone(), done);

                    if done {
                        previous_observation.remove(&env_id);
                        // TODO: hack, change API
                        env_map.get(&env_id).unwrap().send(EnvironmentMessage::Action(0)).unwrap();
                    } else {
                        let epsilon = linear_schedule(config.start_epsilon, config.end_epsilon, config.exploration_fraction * config.total_timesteps as f32, global_step);

                        let action = if rng.gen::<f32>() < epsilon {
                            random_action_sampler.sample(&mut rng)
                        } else {
                            let data = Data::new(obs.clone(), Shape::new([1, obs.len()]));
                            let input = Tensor::<MyAutodiffBackend, 2>::from_data(data);
                            let action = q_network.forward(input).argmax(1).into_scalar();
                            action
                        };

                        previous_observation.insert(env_id.clone(), (obs.clone(), action));

                        env_map.get(&env_id).unwrap().send(EnvironmentMessage::Action(action)).unwrap();
                    }

                    if global_step > config.learning_starts {
                        if global_step % config.train_frequency == 0 {
                            let (obs, actions, rewards, next_obs, dones) = replay_buffer.sample(config.batch_size);

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

                        if global_step % config.target_network_frequency == 0 {
                            target_network = q_network.clone();
                        }
                    }
                }
            }
            global_step += 1;
        }
    });

    sender
}

// TODO: probably a better way to do this
fn vecs_to_tensor(input: Vec<Vec<f32>>) -> Tensor<MyAutodiffBackend, 2> {
    let input_vectors = input.iter().map(|v|
        {
            let data = Data::new(v.clone(), Shape::new([1, v.len()]));
            Tensor::<MyAutodiffBackend, 2>::from_data(data)
        }).collect::<Vec<Tensor<MyAutodiffBackend, 2>>>();
    let t = Tensor::cat(input_vectors, 0);
    t
}
