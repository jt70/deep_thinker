package org.deep_thinker.serde;

import org.deep_thinker.model.GetFirstActionFlat;

import java.nio.ByteBuffer;

public class GetFirstActionFlatSerde implements FlatSerde<GetFirstActionFlat> {
    @Override
    public byte[] serialize(GetFirstActionFlat value) {
        return value.getByteBuffer().array();
    }

    @Override
    public GetFirstActionFlat deserialize(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return GetFirstActionFlat.getRootAsGetFirstActionFlat(buffer);
    }
}
