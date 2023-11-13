use rand::distributions::Uniform;
use rand::prelude::*;
use std::f32::consts::PI;
use deep_thinker::model::Environment;

pub struct Cartpole {
    pub state: Vec<f32>,
    pub step_number: u32,
}

const GRAVITY: f32 = 9.8;
const FORCE_MAG: f32 = 10.0;
const CART_MASS: f32 = 1.0;
const POLE_MASS: f32 = 0.1;
const TOTAL_MASS: f32 = POLE_MASS + CART_MASS;
const LENGTH: f32 = 0.5;
const POLE_MASS_LENGTH: f32 = POLE_MASS * LENGTH;
const TAU: f32 = 0.02;
const X_THRESHOLD: f32 = 2.4;
const THETA_THRESHOLD_RADIANS: f32 = 12.0 * 2.0 * PI / 360.0;
const MAX_STEP: u32 = 500;

// https://coneural.org/florian/papers/05_cart_pole.pdf
impl Environment for Cartpole {
    fn reset(&mut self) -> Vec<f32> {
        let initial_state: Vec<f32> = Uniform::new(-0.05, 0.05).sample_iter(&mut thread_rng()).take(4).collect();
        self.state = initial_state;
        self.step_number = 0;
        self.state.clone()
    }

    fn step(&mut self, action: i32) -> (Vec<f32>, f32, bool) {
        let mut x = self.state[0];
        let mut x_dot = self.state[1];
        let mut theta = self.state[2];
        let mut theta_dot = self.state[3];

        let force = if action == 1 { FORCE_MAG } else { -FORCE_MAG };

        let cos_theta = theta.cos();
        let sin_theta = theta.sin();

        let temp = force + (POLE_MASS_LENGTH * theta_dot.powi(2) * sin_theta) / TOTAL_MASS;
        let theta_acc = (GRAVITY * sin_theta - cos_theta * temp) / (
            LENGTH * (4.0 / 3.0 - POLE_MASS * cos_theta.powi(2) / TOTAL_MASS));
        let x_acc = temp - POLE_MASS_LENGTH * theta_acc * cos_theta / TOTAL_MASS;

        x += TAU * x_dot;
        x_dot += TAU * x_acc;
        theta += TAU * theta_dot;
        theta_dot += TAU * theta_acc;

        self.state = vec![x, x_dot, theta, theta_dot];
        self.step_number += 1;

        let terminated = x < -X_THRESHOLD || x > X_THRESHOLD || theta < -THETA_THRESHOLD_RADIANS || theta > THETA_THRESHOLD_RADIANS || self.step_number >= MAX_STEP;

        (self.state.clone(), 1.0, terminated)
    }
}

impl Cartpole {
    pub fn new() -> Self {
        Self {
            state: vec![0.0, 0.0, 0.0, 0.0],
            step_number: 0,
        }
    }
}
