use std::collections::HashMap;
use async_std::task::sleep;
use clap::{App, Arg};
use futures::prelude::*;
use futures::select;
use std::convert::TryFrom;
use std::time::Duration;
use zenoh::config::Config;
use zenoh::prelude::r#async::*;
use deep_thinker_api::model::{AgentMessage, DQNExperiment};
use deep_thinker::agents::dqn::local_dqn_agent;
use crossbeam_channel::Sender;

fn handle_create_agent_message(s: Sample, agents: &mut HashMap<String, Sender<AgentMessage>>) {
    println!("{}", s);
    let sample_string = s.value.to_string();
    let experiment: DQNExperiment = serde_json::from_str(&sample_string).unwrap();

    println!(">> [Subscriber] Received {} ('{}': '{}')", s.kind, s.key_expr.as_str(), s.value);

    if !agents.contains_key(&experiment.id) {
        let sender = local_dqn_agent(experiment.config);

        agents.insert(experiment.id.clone(), sender);
        println!("Added agent {}", experiment.id);
    }
}

fn handle_spawn_env_message(s: Sample, key: &str, agents: &mut HashMap<String, Sender<AgentMessage>>) {
    let env_id = s.value.to_string();
    let agent_id = key.split("/").last().unwrap().to_string();
    let sender = agents.get(&agent_id).unwrap();
//    spawn_environment(agent_id, env_id, sender.clone(), 500);

    println!("{}", s);
    let sample_string = s.value.to_string();
    let experiment: DQNExperiment = serde_json::from_str(&sample_string).unwrap();

    println!(">> [Subscriber] Received {} ('{}': '{}')", s.kind, s.key_expr.as_str(), s.value);

    if !agents.contains_key(&experiment.id) {
        let sender = local_dqn_agent(experiment.config);

        agents.insert(experiment.id.clone(), sender);
        println!("Added agent {}", experiment.id);
    }
}

#[async_std::main]
async fn main() {
    // Initiate logging
    env_logger::init();

    let (config, key_expr) = parse_args();

    println!("Opening session...");
    let session = zenoh::open(config).res().await.unwrap();

    println!("Declaring Subscriber on '{}'...", &key_expr);

    let subscriber = session.declare_subscriber(&key_expr).res().await.unwrap();

    let mut agents = HashMap::new();

    println!("Enter 'q' to quit...");
    let mut stdin = async_std::io::stdin();
    let mut input = [0_u8];
    loop {
        select!(
            sample = subscriber.recv_async() => {
                let s = sample.unwrap();
                let key: &str = s.key_expr.as_str();

                if key.starts_with("agent_server/dqn/create_agent") {
                    handle_create_agent_message(s, &mut agents);
                } else if key.starts_with("agent_server/dqn/spawn_env") {
                    handle_spawn_env_message(s, key, &mut agents);
                } else {
                    println!(">> [Subscriber] Received {} ('{}': '{}')", s.kind, s.key_expr.as_str(), s.value);
                }
            },

            _ = stdin.read_exact(&mut input).fuse() => {
                match input[0] {
                    b'q' => break,
                    0 => sleep(Duration::from_secs(1)).await,
                    _ => (),
                }
            }
        );
    }
}

fn parse_args() -> (Config, KeyExpr<'static>) {
    let args = App::new("zenoh sub example")
        .arg(
            Arg::from_usage("-m, --mode=[MODE]  'The zenoh session mode (peer by default).")
                .possible_values(["peer", "client"]),
        )
        .arg(Arg::from_usage(
            "-e, --connect=[ENDPOINT]...   'Endpoints to connect to.'",
        ))
        .arg(Arg::from_usage(
            "-l, --listen=[ENDPOINT]...   'Endpoints to listen on.'",
        ))
        .arg(
            Arg::from_usage("-k, --key=[KEYEXPR] 'The key expression to subscribe to.'")
                .default_value("agent_server/dqn/**"),
        )
        .arg(Arg::from_usage(
            "-c, --config=[FILE]      'A configuration file.'",
        ))
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

    let key_expr = KeyExpr::try_from(args.value_of("key").unwrap())
        .unwrap()
        .into_owned();

    (config, key_expr)
}