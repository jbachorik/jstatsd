package io.btrace.jstatsd.server.queues;

import org.jctools.queues.MessagePassingQueue;

import java.util.concurrent.atomic.AtomicBoolean;

public final class ExitCondition implements MessagePassingQueue.ExitCondition {
    private final AtomicBoolean running = new AtomicBoolean(true);

    @Override
    public boolean keepRunning() {
        return running.get();
    }

    public void signal() {
        running.compareAndSet(true, false);
    }
}
