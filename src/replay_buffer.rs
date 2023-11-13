use rand::Rng;

pub struct ReplayBuffer {
    buffer_size: usize,
    obs: Vec<Vec<f32>>,
    actions: Vec<i32>,
    rewards: Vec<f32>,
    next_obs: Vec<Vec<f32>>,
    dones: Vec<f32>,
}

impl ReplayBuffer {
    // TODO: use VecDeque instead of Vec
    pub fn new(buffer_size: usize) -> Self {
        Self {
            buffer_size,
            obs: Vec::with_capacity(buffer_size),
            actions: Vec::with_capacity(buffer_size),
            rewards: Vec::with_capacity(buffer_size),
            next_obs: Vec::with_capacity(buffer_size),
            dones: Vec::with_capacity(buffer_size),
        }
    }

    pub fn add(&mut self, obs: Vec<f32>, action: i32, reward: f32, next_obs: Vec<f32>, done: bool) {
        self.obs.push(obs);
        self.actions.push(action);
        self.rewards.push(reward);
        self.next_obs.push(next_obs);
        self.dones.push(if done { 1.0 } else { 0.0 });

        if self.obs.len() > self.buffer_size {
            self.obs.remove(0);
            self.actions.remove(0);
            self.rewards.remove(0);
            self.next_obs.remove(0);
            self.dones.remove(0);
        }
    }

    pub fn sample(&mut self, batch_size: usize) -> (Vec<Vec<f32>>, Vec<i32>, Vec<f32>, Vec<Vec<f32>>, Vec<f32>) {
        let mut rng = rand::thread_rng();
        let indices: Vec<usize> = (0..batch_size).map(|_| rng.gen_range(0..self.obs.len())).collect();

        let obs = indices.iter().map(|&i| self.obs[i].clone()).collect::<Vec<Vec<f32>>>();
        let actions = indices.iter().map(|&i| self.actions[i]).collect::<Vec<i32>>();
        let rewards = indices.iter().map(|&i| self.rewards[i]).collect::<Vec<f32>>();
        let next_obs = indices.iter().map(|&i| self.next_obs[i].clone()).collect::<Vec<Vec<f32>>>();
        let dones = indices.iter().map(|&i| self.dones[i]).collect::<Vec<f32>>();

        (obs, actions, rewards, next_obs, dones)
    }
}

