package org.deep_thinker.serde;

import org.deep_thinker.model.ResponseMetadata;

import java.nio.ByteBuffer;

public class ResponseMetadataSerde implements FlatSerde<ResponseMetadata> {
    @Override
    public byte[] serialize(ResponseMetadata value) {
        return value.getByteBuffer().array();
    }

    @Override
    public ResponseMetadata deserialize(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return ResponseMetadata.getRootAsResponseMetadata(buffer);
    }
}
