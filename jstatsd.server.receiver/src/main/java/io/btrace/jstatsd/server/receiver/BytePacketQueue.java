package io.btrace.jstatsd.server.receiver;

import org.jctools.queues.SpmcArrayQueue;

import javax.inject.Singleton;

@Singleton
public final class BytePacketQueue extends SpmcArrayQueue<byte[]> {
    private static final int QUEUE_SIZE = 3000;

    public BytePacketQueue() {
        super(QUEUE_SIZE);
    }
}
