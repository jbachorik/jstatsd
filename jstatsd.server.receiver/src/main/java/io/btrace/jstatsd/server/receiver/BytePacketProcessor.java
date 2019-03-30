package io.btrace.jstatsd.server.receiver;

import io.btrace.jstatsd.server.ledger.LedgerScribbler;
import io.btrace.jstatsd.server.queues.ProgressiveWaitStrategy;

import javax.inject.Inject;

/**
 * A {@link BytePacketQueue} skimmer with the sole responsibility of
 * establishing connection between the queue and a {@link LedgerScribbler}
 * instance.
 */
public final class BytePacketProcessor implements Runnable {
    private final BytePacketQueue queue;
    private final ProgressiveWaitStrategy wStrategy;
    private final LedgerScribbler transformer;

    @Inject
    BytePacketProcessor(BytePacketQueue q, LedgerScribbler pt, ProgressiveWaitStrategy w) {
        this.queue = q;
        this.wStrategy = w;
        this.transformer = pt;
    }

    @Override
    public void run() {
        queue.drain(transformer, wStrategy, wStrategy.getXCondition());

        System.out.println("--- remaining " + queue.size() + " items on queue");
    }

    void onStop() {
        wStrategy.signalStop();
    }
}
