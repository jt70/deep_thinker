package org.deep_thinker.serde;

import org.deep_thinker.model.DQNConfig;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

public interface MessagePackSerde<T> {
    void serialize(MessageBufferPacker packer, T value);
    T deserialize(MessageUnpacker unpacker);
}
