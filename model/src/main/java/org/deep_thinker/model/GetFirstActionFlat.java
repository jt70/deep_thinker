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
public final class GetFirstActionFlat extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_5_26(); }
  public static GetFirstActionFlat getRootAsGetFirstActionFlat(ByteBuffer _bb) { return getRootAsGetFirstActionFlat(_bb, new GetFirstActionFlat()); }
  public static GetFirstActionFlat getRootAsGetFirstActionFlat(ByteBuffer _bb, GetFirstActionFlat obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public GetFirstActionFlat __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public String envId() { int o = __offset(4); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer envIdAsByteBuffer() { return __vector_as_bytebuffer(4, 1); }
  public ByteBuffer envIdInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 4, 1); }
  public float state(int j) { int o = __offset(6); return o != 0 ? bb.getFloat(__vector(o) + j * 4) : 0; }
  public int stateLength() { int o = __offset(6); return o != 0 ? __vector_len(o) : 0; }
  public FloatVector stateVector() { return stateVector(new FloatVector()); }
  public FloatVector stateVector(FloatVector obj) { int o = __offset(6); return o != 0 ? obj.__assign(__vector(o), bb) : null; }
  public ByteBuffer stateAsByteBuffer() { return __vector_as_bytebuffer(6, 4); }
  public ByteBuffer stateInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 6, 4); }

  public static int createGetFirstActionFlat(FlatBufferBuilder builder,
      int envIdOffset,
      int stateOffset) {
    builder.startTable(2);
    GetFirstActionFlat.addState(builder, stateOffset);
    GetFirstActionFlat.addEnvId(builder, envIdOffset);
    return GetFirstActionFlat.endGetFirstActionFlat(builder);
  }

  public static void startGetFirstActionFlat(FlatBufferBuilder builder) { builder.startTable(2); }
  public static void addEnvId(FlatBufferBuilder builder, int envIdOffset) { builder.addOffset(0, envIdOffset, 0); }
  public static void addState(FlatBufferBuilder builder, int stateOffset) { builder.addOffset(1, stateOffset, 0); }
  public static int createStateVector(FlatBufferBuilder builder, float[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addFloat(data[i]); return builder.endVector(); }
  public static void startStateVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static int endGetFirstActionFlat(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public GetFirstActionFlat get(int j) { return get(new GetFirstActionFlat(), j); }
    public GetFirstActionFlat get(GetFirstActionFlat obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}

