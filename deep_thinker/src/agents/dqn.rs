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
use rand::prelude::ThreadRng;
use rand::Rng;
use crate::replay_buffer::ReplayBuffer;

type MyBackend = WgpuBackend<AutoGraphicsApi, f32, i32>;
type MyAutodiffBackend = ADBackendDecorator<MyBackend>;

pub struct DQNConfig {
    pub start_epsilon: f32,
    pub end_epsilon: f32,
    pub exploration_fraction: f32,
    pub learning_starts: i32,
    pub train_frequency: i32,
    pub batch_size: usize,
    pub learning_rate: f32,
    pub target_network_frequency: i32,
    pub gamma: f32,
    pub num_actions: usize,
    pub num_inputs: i32,
    pub hidden_1_size: i32,
    pub hidden_2_size: i32,
    pub total_timesteps: u64,
}

pub struct DQNAgent {
    learning_rate: LearningRate,
    random_action_sampler: Uniform<i32>,
    rand: ThreadRng,
    start_epsilon: f32,
    end_epsilon: f32,
    exploration_fraction: f32,
    total_timesteps: u64,
    global_step: u64,
    q_network: QNetwork<MyAutodiffBackend>,
    target_network: QNetwork<MyAutodiffBackend>,
    rb: ReplayBuffer,
    //optim: dyn Optimizer<MyAutodiffBackend, QNetwork<MyAutodiffBackend>, Record=()>,
}

impl DQNAgent {
    pub fn new(config: DQNConfig) -> Self {
        //    let gamma = Tensor::<MyAutodiffBackend, 2>::full([batch_size, 1], 0.99);
        let ones = Tensor::<MyAutodiffBackend, 2>::full([config.batch_size, 1], 1.0);

        let random_action_sampler = Uniform::from(0..config.num_actions as i32);
        let rand = rand::thread_rng();
        let mut q_network = QNetwork::<MyAutodiffBackend>::new(4, 120, 84, config.num_actions);

        let optimizer = AdamConfig::new();
        let mut optim = optimizer.init::<MyAutodiffBackend, QNetwork<MyAutodiffBackend>>();

//        let mut optim: dyn Optimizer<MyAutodiffBackend, QNetwork<MyAutodiffBackend>> = optimizer.init();
        let mut target_network = q_network.clone();

        let mut rb = ReplayBuffer::new(10_000);

        DQNAgent {
            learning_rate: LearningRate::from(config.learning_rate),
            random_action_sampler,
            rand,
            start_epsilon: config.start_epsilon,
            end_epsilon: config.end_epsilon,
            exploration_fraction: config.exploration_fraction,
            total_timesteps: config.total_timesteps,
            global_step: 0,
            q_network,
            target_network,
            rb,
            //optim,
        }
    }

    pub fn get_first_action(&self, obs: Vec<f32>) -> i32 {
        self.random_action_sampler.sample(&mut rand::thread_rng())
    }

    pub fn end_episode(&self, obs: Vec<f32>, reward: f32) {
    }

    pub fn get_action(&mut self, obs: Vec<f32>, reward: f32) -> i32 {
        let epsilon = linear_schedule(self.start_epsilon, self.end_epsilon, self.exploration_fraction * self.total_timesteps as f32, self.global_step);

        let action = if self.rand.gen::<f32>() < epsilon {
            self.random_action_sampler.sample(&mut self.rand)
        } else {
            let data = Data::new(obs.clone(), Shape::new([1, obs.len()]));
            let input = Tensor::<MyAutodiffBackend, 2>::from_data(data);
            let action = self.q_network.forward(input).argmax(1).into_scalar();
            action
        };

        action
    }


}

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

fn linear_schedule(start_e: f32, end_e: f32, duration: f32, t: u64) -> f32 {
    let slope = (end_e - start_e) / duration;
    end_e.max(slope * t as f32 + start_e)
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