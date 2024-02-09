package org.deep_thinker.serde;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;

public class IntMessagePackSerde implements MessagePackSerde<Integer> {
    @Override
    public void serialize(MessageBufferPacker packer, Integer value) {
        try {
            packer.packInt(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Integer deserialize(MessageUnpacker unpacker) {
        try {
            return unpacker.unpackInt();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
