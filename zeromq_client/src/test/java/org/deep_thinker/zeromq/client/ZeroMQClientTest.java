package org.deep_thinker.zeromq.client;

import org.junit.jupiter.api.Test;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ZeroMQClientTest {
    @Test
    public void testToUpperCase() throws ExecutionException, InterruptedException, TimeoutException {
        ZeroMQClient zeroMQClient = new ZeroMQClient();
        String upper = zeroMQClient.toUpperCase("hello").get(5, TimeUnit.SECONDS);
        assertEquals("HELLO", upper);
    }

    @Test
    public void testMessagePack() throws IOException {
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        packer.packInt(1000);

        byte[] ba1 = new byte[]{1, 2};
        packer.packBinaryHeader(ba1.length);
        packer.writePayload(ba1);

        packer.packInt(2000);

        byte[] ba2 = new byte[]{3, 4, 5};
        packer.packBinaryHeader(ba2.length);
        packer.writePayload(ba2);

        packer.close();

        byte[] bytes = packer.toByteArray();

        MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(bytes);

        int int1 = unpacker.unpackInt();
        byte[] ba1Unpacked = unpacker.readPayload(unpacker.unpackBinaryHeader());
        int int2 = unpacker.unpackInt();
        byte[] ba2Unpacked = unpacker.readPayload(unpacker.unpackBinaryHeader());

        assertEquals(1000, int1);
        assertEquals(2, ba1Unpacked.length);
        assertEquals(1, ba1Unpacked[0]);
        assertEquals(2, ba1Unpacked[1]);
        assertEquals(2000, int2);
        assertEquals(3, ba2Unpacked.length);
        assertEquals(3, ba2Unpacked[0]);
        assertEquals(4, ba2Unpacked[1]);
        assertEquals(5, ba2Unpacked[2]);
    }

//    @Test
//    public void testMessagePackPerformance() throws IOException {
//        long start = System.currentTimeMillis();
//        for (int i = 0; i < 10000000; i++) {
//            MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
//            packer.packInt(1000);
//            packer.packString("hello world");
//            packer.packFloat(10.0f);
//            byte[] b = packer.toByteArray();
//
////            MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(b);
////            unpacker.unpackInt();
////            unpacker.unpackString();
////            unpacker.unpackFloat();
//        }
//        long end = System.currentTimeMillis();
//        System.out.println("MessagePack performance: " + (end - start) + "ms");
//    }
}
