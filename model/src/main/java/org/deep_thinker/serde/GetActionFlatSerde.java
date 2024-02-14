package org.deep_thinker.serde;

import org.deep_thinker.model.GetActionFlat;

import java.nio.ByteBuffer;

public class GetActionFlatSerde implements FlatSerde<GetActionFlat> {
    @Override
    public byte[] serialize(GetActionFlat value) {
        return value.getByteBuffer().array();
    }

    @Override
    public GetActionFlat deserialize(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return GetActionFlat.getRootAsGetActionFlat(buffer);
    }
}
