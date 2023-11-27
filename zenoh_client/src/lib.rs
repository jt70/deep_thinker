use std::sync::Arc;
use zenoh::Session;
use deep_thinker_api::model::{Agent, DQNExperiment};
use zenoh::prelude::r#sync::*;

pub async fn zenoh_dqn_agent(dqn_experiment: DQNExperiment, session: Arc<Session>) {
    let serialized = serde_json::to_string(&dqn_experiment).unwrap();

    println!("Starting remote agent at ('agent_server/dqn': '{serialized}')...");
    session.put("agent_server/dqn/create_agent", serialized).res().unwrap();
}

pub struct ZenohAgentProxy {
    pub session: Arc<Session>,
    pub env_id: String,
}

impl ZenohAgentProxy {
    // send message to the agent to link the environment
    pub fn new(agent_id: String, env_id: String, session: Arc<Session>) -> ZenohAgentProxy {
        session.put(format!("agent_server/dqn/spawn_env/{}", agent_id), env_id.clone()).res().unwrap();

        ZenohAgentProxy {
            session,
            env_id,
        }
    }
}

impl Agent for ZenohAgentProxy {
    fn get_first_action(&self, _observation: Vec<f32>) -> i32 {
        //let subscriber = self.session.declare_subscriber("&key_expr").res().unwrap();


        0
        // self.sender.send(AgentMessage::GetFirstAction {
        //     env_id: self.env_id.clone(),
        //     obs: observation,
        // }).unwrap();
        // let env_message = self.receiver.recv().unwrap();
        // match env_message {
        //     EnvironmentMessage::Action(action) => action,
        //     _ => panic!("Unexpected action"),
        // }
    }

    fn get_action(&self, _observation: Vec<f32>, _reward: f32, _done: bool) -> i32 {
        0
        // self.sender.send(AgentMessage::GetAction {
        //     env_id: self.env_id.clone(),
        //     obs: observation,
        //     reward,
        //     done,
        // }).unwrap();
        // let env_message = self.receiver.recv().unwrap();
        // match env_message {
        //     EnvironmentMessage::Action(action) => action,
        //     _ => panic!("Unexpected action"),
        // }
    }
}
