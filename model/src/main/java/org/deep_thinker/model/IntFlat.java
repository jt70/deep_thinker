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
public final class IntFlat extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_5_26(); }
  public static IntFlat getRootAsIntFlat(ByteBuffer _bb) { return getRootAsIntFlat(_bb, new IntFlat()); }
  public static IntFlat getRootAsIntFlat(ByteBuffer _bb, IntFlat obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public IntFlat __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public int value() { int o = __offset(4); return o != 0 ? bb.getInt(o + bb_pos) : 0; }

  public static int createIntFlat(FlatBufferBuilder builder,
      int value) {
    builder.startTable(1);
    IntFlat.addValue(builder, value);
    return IntFlat.endIntFlat(builder);
  }

  public static void startIntFlat(FlatBufferBuilder builder) { builder.startTable(1); }
  public static void addValue(FlatBufferBuilder builder, int value) { builder.addInt(0, value, 0); }
  public static int endIntFlat(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public IntFlat get(int j) { return get(new IntFlat(), j); }
    public IntFlat get(IntFlat obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}

