package org.deep_thinker.serde;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;

public class StringMessagePackSerde implements MessagePackSerde<String> {
    @Override
    public void serialize(MessageBufferPacker packer, String value) {
        try {
            packer.packString(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String deserialize(MessageUnpacker unpacker) {
        try {
            return unpacker.unpackString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
