package org.deep_thinker.zeromq.client;

import org.junit.jupiter.api.Test;

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
        Thread.sleep(1000);
    }
}
