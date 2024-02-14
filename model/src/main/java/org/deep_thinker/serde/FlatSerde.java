package org.deep_thinker.serde;

public interface FlatSerde<T> {
    byte[] serialize(T value);

    T deserialize(byte[] bytes);
}
