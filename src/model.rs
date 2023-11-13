pub trait Environment {
    fn reset(&mut self) -> Vec<f32>;
    fn step(&mut self, action: i32) -> (Vec<f32>, f32, bool);
}

