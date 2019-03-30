package io.btrace.jstatsd.server.api.execution;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class DaemonThreadFactory implements ThreadFactory {
    private final String name;
    private final boolean counted;

    private final AtomicInteger counter = new AtomicInteger(1);
    public DaemonThreadFactory(String name) {
        this(name, false);
    }

    public DaemonThreadFactory(String name, boolean counted) {
        this.name = name;
        this.counted = counted;
    }

    @Override
    public Thread newThread(Runnable r) {
        String tName = name;
        if (counted) {
            tName = tName + "[" + counter.getAndIncrement() + "]";
        }
        Thread t = new Thread(r, tName);
        t.setDaemon(true);
        return t;
    }
}
