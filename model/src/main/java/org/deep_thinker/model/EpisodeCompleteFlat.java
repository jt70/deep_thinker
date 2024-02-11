// automatically generated by the FlatBuffers compiler, do not modify

package org.deep_thinker.model;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.BooleanVector;
import com.google.flatbuffers.ByteVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.DoubleVector;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.FloatVector;
import com.google.flatbuffers.IntVector;
import com.google.flatbuffers.LongVector;
import com.google.flatbuffers.ShortVector;
import com.google.flatbuffers.StringVector;
import com.google.flatbuffers.Struct;
import com.google.flatbuffers.Table;
import com.google.flatbuffers.UnionVector;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class EpisodeCompleteFlat extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_5_26(); }
  public static EpisodeCompleteFlat getRootAsEpisodeCompleteFlat(ByteBuffer _bb) { return getRootAsEpisodeCompleteFlat(_bb, new EpisodeCompleteFlat()); }
  public static EpisodeCompleteFlat getRootAsEpisodeCompleteFlat(ByteBuffer _bb, EpisodeCompleteFlat obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public EpisodeCompleteFlat __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public String envId() { int o = __offset(4); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer envIdAsByteBuffer() { return __vector_as_bytebuffer(4, 1); }
  public ByteBuffer envIdInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 4, 1); }
  public float state(int j) { int o = __offset(6); return o != 0 ? bb.getFloat(__vector(o) + j * 4) : 0; }
  public int stateLength() { int o = __offset(6); return o != 0 ? __vector_len(o) : 0; }
  public FloatVector stateVector() { return stateVector(new FloatVector()); }
  public FloatVector stateVector(FloatVector obj) { int o = __offset(6); return o != 0 ? obj.__assign(__vector(o), bb) : null; }
  public ByteBuffer stateAsByteBuffer() { return __vector_as_bytebuffer(6, 4); }
  public ByteBuffer stateInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 6, 4); }
  public float reward() { int o = __offset(8); return o != 0 ? bb.getFloat(o + bb_pos) : 0.0f; }
  public float episodeReward() { int o = __offset(10); return o != 0 ? bb.getFloat(o + bb_pos) : 0.0f; }

  public static int createEpisodeCompleteFlat(FlatBufferBuilder builder,
      int envIdOffset,
      int stateOffset,
      float reward,
      float episodeReward) {
    builder.startTable(4);
    EpisodeCompleteFlat.addEpisodeReward(builder, episodeReward);
    EpisodeCompleteFlat.addReward(builder, reward);
    EpisodeCompleteFlat.addState(builder, stateOffset);
    EpisodeCompleteFlat.addEnvId(builder, envIdOffset);
    return EpisodeCompleteFlat.endEpisodeCompleteFlat(builder);
  }

  public static void startEpisodeCompleteFlat(FlatBufferBuilder builder) { builder.startTable(4); }
  public static void addEnvId(FlatBufferBuilder builder, int envIdOffset) { builder.addOffset(0, envIdOffset, 0); }
  public static void addState(FlatBufferBuilder builder, int stateOffset) { builder.addOffset(1, stateOffset, 0); }
  public static int createStateVector(FlatBufferBuilder builder, float[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addFloat(data[i]); return builder.endVector(); }
  public static void startStateVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addReward(FlatBufferBuilder builder, float reward) { builder.addFloat(2, reward, 0.0f); }
  public static void addEpisodeReward(FlatBufferBuilder builder, float episodeReward) { builder.addFloat(3, episodeReward, 0.0f); }
  public static int endEpisodeCompleteFlat(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public EpisodeCompleteFlat get(int j) { return get(new EpisodeCompleteFlat(), j); }
    public EpisodeCompleteFlat get(EpisodeCompleteFlat obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}

