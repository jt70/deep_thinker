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
public final class ResponseMetadata extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_5_26(); }
  public static ResponseMetadata getRootAsResponseMetadata(ByteBuffer _bb) { return getRootAsResponseMetadata(_bb, new ResponseMetadata()); }
  public static ResponseMetadata getRootAsResponseMetadata(ByteBuffer _bb, ResponseMetadata obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public ResponseMetadata __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public String responseId() { int o = __offset(4); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer responseIdAsByteBuffer() { return __vector_as_bytebuffer(4, 1); }
  public ByteBuffer responseIdInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 4, 1); }

  public static int createResponseMetadata(FlatBufferBuilder builder,
      int responseIdOffset) {
    builder.startTable(1);
    ResponseMetadata.addResponseId(builder, responseIdOffset);
    return ResponseMetadata.endResponseMetadata(builder);
  }

  public static void startResponseMetadata(FlatBufferBuilder builder) { builder.startTable(1); }
  public static void addResponseId(FlatBufferBuilder builder, int responseIdOffset) { builder.addOffset(0, responseIdOffset, 0); }
  public static int endResponseMetadata(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public ResponseMetadata get(int j) { return get(new ResponseMetadata(), j); }
    public ResponseMetadata get(ResponseMetadata obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}

