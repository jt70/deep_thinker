package org.deep_thinker.model;

import java.util.concurrent.CompletableFuture;

public interface DeepThinkerClient {
    CompletableFuture<String> toUpperCase(String s);
    CompletableFuture<Void> createDQNAgent(DQNConfig config);
    CompletableFuture<Integer> getFirstAction(String agentId, String envId, float[] state);
    CompletableFuture<Integer> getAction(String agentId, String envId, float[] state, float reward);
    CompletableFuture<Void> episodeComplete(String agentId, String envId, float[] state, float reward, float episodeReward);
}
