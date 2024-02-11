package org.deep_thinker.serde;

import org.deep_thinker.model.EpisodeCompleteFlat;
import org.deep_thinker.model.GetActionFlat;

import java.nio.ByteBuffer;

public class EpisodeCompleteFlatSerde implements FlatSerde<EpisodeCompleteFlat> {
    @Override
    public byte[] serialize(EpisodeCompleteFlat value) {
        return value.getByteBuffer().array();
    }

    @Override
    public EpisodeCompleteFlat deserialize(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return EpisodeCompleteFlat.getRootAsEpisodeCompleteFlat(buffer);
    }
}
