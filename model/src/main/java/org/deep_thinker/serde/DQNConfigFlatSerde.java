package org.deep_thinker.serde;

import org.deep_thinker.model.DQNConfigFlat;

import java.nio.ByteBuffer;

public class DQNConfigFlatSerde implements FlatSerde<DQNConfigFlat> {
    @Override
    public byte[] serialize(DQNConfigFlat value) {
        return value.getByteBuffer().array();
    }

    @Override
    public DQNConfigFlat deserialize(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return DQNConfigFlat.getRootAsDQNConfigFlat(buffer);
    }
}
