package org.deep_thinker.serde;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;

public class VoidSerde implements MessagePackSerde<Void> {
    @Override
    public void serialize(MessageBufferPacker packer, Void value) {
    }

    @Override
    public Void deserialize(MessageUnpacker unpacker) {
        return null;
    }
}
