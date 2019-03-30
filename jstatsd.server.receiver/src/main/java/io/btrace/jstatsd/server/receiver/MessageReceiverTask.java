package io.btrace.jstatsd.server.receiver;

import javax.inject.Inject;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public final class MessageReceiverTask implements Callable<Void> {
    private final MessageReceiverImpl receiver;

    private final AtomicLong pktCounter = new AtomicLong();
    private final AtomicBoolean exitFlag = new AtomicBoolean(false);

    @Inject
    MessageReceiverTask(Set<MessageReceiverImpl> recvs) {
        MessageReceiverImpl mr = null;
        for(MessageReceiverImpl r : recvs) {
            if (r.isEnabled()) {
                mr = r;
                break;
            }
        }
        receiver = mr;
    }

    public final void stop() {
        exitFlag.set(true);
    }

    @Override
    public final Void call() throws Exception {
        pktCounter.set(0);
        receiver.setup();

        while (!(exitFlag.get() || Thread.currentThread().isInterrupted())) {
            pktCounter.getAndAdd(receiver.receiveMessage());
        }
        return null;
    }

    long getReceivedPackets() {
        return pktCounter.get();
    }
}
