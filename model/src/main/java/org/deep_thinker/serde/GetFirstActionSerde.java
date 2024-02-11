package org.deep_thinker.serde;

import org.deep_thinker.model.GetFirstAction;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;

public class GetFirstActionSerde implements MessagePackSerde<GetFirstAction> {
    @Override
    public void serialize(MessageBufferPacker packer, GetFirstAction value) {
        try {
            packer.packString(value.getEnvId());
            //packer.packFloat(value.getState());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GetFirstAction deserialize(MessageUnpacker unpacker) {
        return null;
    }
}
