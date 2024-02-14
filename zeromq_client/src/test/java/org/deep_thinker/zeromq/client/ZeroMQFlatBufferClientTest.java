package org.deep_thinker.zeromq.client;

import org.deep_thinker.model.DeepThinkerClient;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ZeroMQFlatBufferClientTest {
    @Test
    public void testToUpperCase() throws ExecutionException, InterruptedException, TimeoutException {
        DeepThinkerClient zeroMQClient = new ZeroMQFlatBufferClient();
        String upper = zeroMQClient.toUpperCase("hello").get(5, TimeUnit.SECONDS);
        assertEquals("HELLO", upper);
    }
}
