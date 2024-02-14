package org.deep_thinker.serde;

import com.google.flatbuffers.FlatBufferBuilder;
import org.deep_thinker.model.IntFlat;

import java.nio.ByteBuffer;

public class IntFlatSerde implements FlatSerde<Integer> {
    @Override
    public byte[] serialize(Integer value) {
        FlatBufferBuilder builder = new FlatBufferBuilder(20);
        builder.finish(IntFlat.createIntFlat(builder, value));
        return builder.sizedByteArray();
    }

    @Override
    public Integer deserialize(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        var intFlat = IntFlat.getRootAsIntFlat(buffer);
        return intFlat.value();
    }
}
