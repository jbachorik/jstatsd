package io.btrace.jstatsd.server.queues;

import org.jctools.queues.MessagePassingQueue;

import javax.inject.Inject;

public final class ProgressiveWaitStrategy implements MessagePassingQueue.WaitStrategy {
    private final ExitCondition xCondition;

    @Inject
    public ProgressiveWaitStrategy(ExitCondition cond) {
        this.xCondition = cond;
    }

    @Override
    public int idle(int i) {
        if (!xCondition.keepRunning()) return 0;
        try {
            if (i < 3000) {
                Thread.yield();
            } else if (i < 3100) {
                Thread.sleep(1);
            } else {
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
        }
        return i + 1;
    }

    public void signalStop() {
        xCondition.signal();
    }

    public ExitCondition getXCondition() {
        return xCondition;
    }
}
