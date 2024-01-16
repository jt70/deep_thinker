package org.deep_thinker.env.cartpole;

import org.deep_thinker.model.Environment;
import org.deep_thinker.model.Step;

import java.util.function.Supplier;

import static java.lang.Math.PI;

public class Cartpole implements Environment<Integer, float[]> {
    private static Float GRAVITY = 9.8f;
    private static Float FORCE_MAG = 10.0f;
    private static Float CART_MASS = 1.0f;
    private static Float POLE_MASS = 0.1f;
    private static Float TOTAL_MASS = POLE_MASS + CART_MASS;
    private static Float LENGTH = 0.5f;
    private static Float POLE_MASS_LENGTH = POLE_MASS * LENGTH;
    private static Float TAU = 0.02f;
    private static Float X_THRESHOLD = 2.4f;
    private static Float THETA_THRESHOLD_RADIANS = 12.0f * 2.0f * (float) PI / 360.0f;
    private static Integer MAX_STEP = 500;

    private Integer step = 0;
    private float[] state = new float[]{0.0f, 0.0f, 0.0f, 0.0f};

    @Override
    public float[] reset() {
        Supplier<Float> randFloat = () -> (float) Math.random() * 0.1f - 0.05f;

        step = 0;
        state = new float[]{randFloat.get(), randFloat.get(), randFloat.get(), randFloat.get()};
        return state;
    }

    @Override
    public Step step(Integer action) {
        var x = state[0];
        var x_dot = state[1];
        var theta = state[2];
        var theta_dot = state[3];

        var force = (action == 1) ? FORCE_MAG : -FORCE_MAG;

        var cos_theta = Math.cos(theta);
        var sin_theta = Math.sin(theta);

        var temp = force + (POLE_MASS_LENGTH * Math.pow(theta_dot, 2.0) * sin_theta) / TOTAL_MASS;
        var theta_acc = (GRAVITY * sin_theta - cos_theta * temp) / (
                LENGTH * (4.0 / 3.0 - POLE_MASS * Math.pow(cos_theta, 2.0) / TOTAL_MASS));
        var x_acc = temp - POLE_MASS_LENGTH * theta_acc * cos_theta / TOTAL_MASS;

        x += TAU * x_dot;
        x_dot += TAU * x_acc;
        theta += TAU * theta_dot;
        theta_dot += TAU * theta_acc;

        state = new float[]{x, x_dot, theta, theta_dot};
        step += 1;

        boolean terminated =
                x < -X_THRESHOLD || x > X_THRESHOLD || theta < -THETA_THRESHOLD_RADIANS || theta > THETA_THRESHOLD_RADIANS || step >= MAX_STEP;

        return new Step(1.0f, terminated, state);
    }
}
