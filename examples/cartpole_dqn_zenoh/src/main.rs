use std::thread;
use crossbeam_channel::Sender;
use deep_thinker_api::model::{Agent, AgentMessage, ChannelAgentProxy, DQNConfig};
use environments::cartpole;
use deep_thinker_api::model::Environment;

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

    let agent_sender = zenoh_dqn_agent("zenoh_run_1", config);

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

fn zenoh_dqn_agent(p0: &str, p1: DQNConfig) -> _ {
    todo!()
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

/*

//
// Copyright (c) 2023 ZettaScale Technology
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License 2.0 which is available at
// http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
// which is available at https://www.apache.org/licenses/LICENSE-2.0.
//
// SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
//
// Contributors:
//   ZettaScale Zenoh Team, <zenoh@zettascale.tech>
//
use clap::{App, Arg};
use zenoh::config::Config;
use zenoh::prelude::r#async::*;
use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize, Debug)]
struct Point {
    x: i32,
    y: i32,
}

#[async_std::main]
async fn main() {
    // initiate logging
    env_logger::init();

    let (config, key_expr, _value) = parse_args();

    println!("Opening session...");
    let session = zenoh::open(config).res().await.unwrap();

    let point = Point { x: 1, y: 2 };

    // Convert the Point to a JSON string.
    let serialized = serde_json::to_string(&point).unwrap();

    println!("Putting string ('{key_expr}': '{serialized}')...");
    session.put(&key_expr, serialized).res().await.unwrap();

    session.close().res().await.unwrap();
}

//
// Argument parsing -- look at the main for the zenoh-related code
//
fn parse_args() -> (Config, String, f64) {
    let default_value = std::f64::consts::PI.to_string();

    let args = App::new("zenoh put float example")
        .arg(
            Arg::from_usage("-m, --mode=[MODE] 'The zenoh session mode (peer by default).")
                .possible_values(["peer", "client"]),
        )
        .arg(Arg::from_usage(
            "-e, --connect=[ENDPOINT]...  'Endpoints to connect to.'",
        ))
        .arg(Arg::from_usage(
            "-l, --listen=[ENDPOINT]...   'Endpoints to listen on.'",
        ))
        .arg(Arg::from_usage(
            "-c, --config=[FILE]      'A configuration file.'",
        ))
        .arg(
            Arg::from_usage("-k, --key=[KEYEXPR]        'The key expression to put.'")
                .default_value("demo/example/zenoh-rs-put"),
        )
        .arg(
            Arg::from_usage("-v, --value=[VALUE]      'The float value to put.'")
                .default_value(&default_value),
        )
        .arg(Arg::from_usage(
            "--no-multicast-scouting 'Disable the multicast-based scouting mechanism.'",
        ))
        .get_matches();

    let mut config = if let Some(conf_file) = args.value_of("config") {
        Config::from_file(conf_file).unwrap()
    } else {
        Config::default()
    };
    if let Some(Ok(mode)) = args.value_of("mode").map(|mode| mode.parse()) {
        config.set_mode(Some(mode)).unwrap();
    }
    if let Some(values) = args.values_of("connect") {
        config.connect.endpoints = values.map(|v| v.parse().unwrap()).collect();
    }
    if let Some(values) = args.values_of("listen") {
        config.listen.endpoints = values.map(|v| v.parse().unwrap()).collect();
    }
    if args.is_present("no-multicast-scouting") {
        config.scouting.multicast.set_enabled(Some(false)).unwrap();
    }

    let key_expr = args.value_of("key").unwrap().to_string();
    let value: f64 = args.value_of("value").unwrap().parse().unwrap();

    (config, key_expr, value)
}

 */