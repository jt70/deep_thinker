use crossbeam_channel::{bounded, Receiver, Sender};
use crate::agents::dqn::spawn_dqn_actor;

pub trait Environment {
    fn reset(&mut self) -> Vec<f32>;
    fn step(&mut self, action: i32) -> (Vec<f32>, f32, bool);
}

pub trait Agent {
    fn get_first_action(&self, observation: Vec<f32>) -> i32;
    fn get_action(&self, observation: Vec<f32>, reward: f32, done: bool) -> i32;
}

pub struct DQNConfig {
    pub start_epsilon: f32,
    pub end_epsilon: f32,
    pub exploration_fraction: f32,
    pub learning_starts: i32,
    pub train_frequency: i32,
    pub batch_size: usize,
    pub learning_rate: f64,
    pub target_network_frequency: i32,
    pub gamma: f32,
    pub num_actions: usize,
    pub num_inputs: usize,
    pub hidden_1_size: usize,
    pub hidden_2_size: usize,
    pub total_timesteps: u64,
}

#[derive(Debug)]
pub enum AgentMessage {
    GetFirstAction {
        env_id: String,
        obs: Vec<f32>,
    },
    GetAction {
        env_id: String,
        obs: Vec<f32>,
        reward: f32,
        done: bool,
    },
}

#[derive(Debug)]
pub enum EnvironmentMessage {
    Action(i32),
}

pub struct ChannelAgentProxy {
    sender: Sender<AgentMessage>,
    receiver: Receiver<EnvironmentMessage>,
    env_id: String,
}

impl Agent for ChannelAgentProxy {
    fn get_first_action(&self, observation: Vec<f32>) -> i32 {
        self.sender.send(AgentMessage::GetFirstAction {
            env_id: self.env_id.clone(),
            obs: observation,
        }).unwrap();
        let env_message = self.receiver.recv().unwrap();
        match env_message {
            EnvironmentMessage::Action(action) => action,
        }
    }

    fn get_action(&self, observation: Vec<f32>, reward: f32, done: bool) -> i32 {
        self.sender.send(AgentMessage::GetAction {
            env_id: self.env_id.clone(),
            obs: observation,
            reward,
            done,
        }).unwrap();
        let env_message = self.receiver.recv().unwrap();
        match env_message {
            EnvironmentMessage::Action(action) => action,
        }
    }
}

pub fn local_dqn_agent(config: DQNConfig) -> ChannelAgentProxy {
    let (agent_sender, agent_receiver) = bounded(1000);
    let (env_sender, env_receiver) = bounded(1000);

    spawn_dqn_actor(config, agent_receiver, env_sender);

    ChannelAgentProxy {
        sender: agent_sender,
        receiver: env_receiver,
        env_id: "env".to_string(),
    }
}
