package org.deep_thinker.serde;

import com.google.flatbuffers.FlatBufferBuilder;
import org.deep_thinker.model.StringFlat;

import java.nio.ByteBuffer;

public class StringFlatSerde implements FlatSerde<String> {
    @Override
    public byte[] serialize(String value) {
        FlatBufferBuilder builder = new FlatBufferBuilder(20);

        int valueStart = builder.createString(value);
        int stringFlat = StringFlat.createStringFlat(builder, valueStart);

        builder.finish(stringFlat);

        return builder.sizedByteArray();
    }

    @Override
    public String deserialize(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        var stringFlat = StringFlat.getRootAsStringFlat(buffer);
        return stringFlat.value();
    }
}
