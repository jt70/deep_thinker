package org.deep_thinker.model;

public class DQNConfig {
    public float startEpsilon;
    public float endEpsilon;
    public float explorationFraction;
    public int learningStarts;
    public int trainFrequency;
    public int batchSize;
    public float learningRate;
    public int targetNetworkFrequency;
    public float gamma;
    public int numInputs;
    public int numActions;
    public int hidden1Size;
    public int hidden2Size;
    public int totalTimesteps;
    public int replayBufferSize;

    public DQNConfig(float startEpsilon,
                     float endEpsilon,
                     float explorationFraction,
                     int learningStarts,
                     int trainFrequency,
                     int batchSize,
                     float learningRate,
                     int targetNetworkFrequency,
                     float gamma,
                     int numInputs,
                     int numActions,
                     int hidden1Size,
                     int hidden2Size,
                     int totalTimesteps,
                     int replayBufferSize) {
        this.startEpsilon = startEpsilon;
        this.endEpsilon = endEpsilon;
        this.explorationFraction = explorationFraction;
        this.learningStarts = learningStarts;
        this.trainFrequency = trainFrequency;
        this.batchSize = batchSize;
        this.learningRate = learningRate;
        this.targetNetworkFrequency = targetNetworkFrequency;
        this.gamma = gamma;
        this.numInputs = numInputs;
        this.numActions = numActions;
        this.hidden1Size = hidden1Size;
        this.hidden2Size = hidden2Size;
        this.totalTimesteps = totalTimesteps;
        this.replayBufferSize = replayBufferSize;
    }
}
