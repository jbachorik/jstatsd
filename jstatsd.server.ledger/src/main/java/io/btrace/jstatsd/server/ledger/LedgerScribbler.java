package io.btrace.jstatsd.server.ledger;

import io.btrace.jstatsd.server.api.LedgerEntry;
import io.btrace.jstatsd.server.parser.LedgerEntries;
import org.jctools.queues.MessagePassingQueue;

import javax.inject.Inject;

/**
 * Will consume the packets stored on {@code BytePacketQueue},
 * transform them to {@linkplain LedgerEntry} instances and forward them
 * for aggregation.
 */
public final class LedgerScribbler implements MessagePassingQueue.Consumer<byte[]> {
    private final LedgerQueue queue;

    @Inject
    LedgerScribbler(LedgerQueue q) {
        this.queue = q;
    }

    @Override
    public void accept(byte[] e) {
        for(LedgerEntry le : new LedgerEntries(e)) {
            if (le != null) {
                queue.offer(le);
            }
        }
    }
}
