package org.deep_thinker.serde;

import org.deep_thinker.model.DQNConfig;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

public class DQNConfigSerde implements MessagePackSerde<DQNConfig> {
    @Override
    public void serialize(MessageBufferPacker packer, DQNConfig value) {
        try {
            packer.packString(value.agentId);
            packer.packFloat(value.startEpsilon);
            packer.packFloat(value.endEpsilon);
            packer.packFloat(value.explorationFraction);
            packer.packInt(value.learningStarts);
            packer.packInt(value.trainFrequency);
            packer.packInt(value.batchSize);
            packer.packFloat(value.learningRate);
            packer.packInt(value.targetNetworkFrequency);
            packer.packFloat(value.gamma);
            packer.packInt(value.numInputs);
            packer.packInt(value.numActions);
            packer.packInt(value.hidden1Size);
            packer.packInt(value.hidden2Size);
            packer.packInt(value.totalTimesteps);
            packer.packInt(value.replayBufferSize);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DQNConfig deserialize(MessageUnpacker unpacker) {
        try {
            String agentId = unpacker.unpackString();
            float startEpsilon = unpacker.unpackFloat();
            float endEpsilon = unpacker.unpackFloat();
            float explorationFraction = unpacker.unpackFloat();
            int learningStarts = unpacker.unpackInt();
            int trainFrequency = unpacker.unpackInt();
            int batchSize = unpacker.unpackInt();
            float learningRate = unpacker.unpackFloat();
            int targetNetworkFrequency = unpacker.unpackInt();
            float gamma = unpacker.unpackFloat();
            int numInputs = unpacker.unpackInt();
            int numActions = unpacker.unpackInt();
            int hidden1Size = unpacker.unpackInt();
            int hidden2Size = unpacker.unpackInt();
            int totalTimesteps = unpacker.unpackInt();
            int replayBufferSize = unpacker.unpackInt();
            DQNConfig config = new DQNConfig(agentId, startEpsilon, endEpsilon, explorationFraction, learningStarts, trainFrequency, batchSize, learningRate, targetNetworkFrequency, gamma, numInputs, numActions, hidden1Size, hidden2Size, totalTimesteps, replayBufferSize);
            return config;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
