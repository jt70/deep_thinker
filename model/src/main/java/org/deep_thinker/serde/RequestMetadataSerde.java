package org.deep_thinker.serde;

import org.deep_thinker.model.RequestMetadata;

import java.nio.ByteBuffer;

public class RequestMetadataSerde implements FlatSerde<RequestMetadata> {
    @Override
    public byte[] serialize(RequestMetadata value) {
        return value.getByteBuffer().array();
    }

    @Override
    public RequestMetadata deserialize(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return RequestMetadata.getRootAsRequestMetadata(buffer);
    }
}
