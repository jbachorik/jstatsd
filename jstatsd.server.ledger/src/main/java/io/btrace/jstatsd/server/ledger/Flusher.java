package io.btrace.jstatsd.server.ledger;

import io.btrace.jstatsd.server.api.Config;
import io.btrace.jstatsd.server.api.Snapshot;
import io.btrace.jstatsd.server.api.StatsReporter;
import io.btrace.jstatsd.server.api.execution.Scheduler;
import io.btrace.jstatsd.server.spi.Sink;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

final class Flusher implements StatsReporter {
    private static final Logger LOG = Logger.getLogger(Flusher.class);

    private final int period;
    private final Scheduler scheduler;
    private final LedgerQueue queue;
    private final Set<Sink> backends = new HashSet<>();

    private volatile ScheduledFuture<?> scheduledWork;

    @Inject
    Flusher(LedgerQueue q, Set<Sink> backends, Scheduler s, Config cfg) {
        this.period = cfg.getFlushPeriod();
        this.scheduler = s;
        this.queue = q;
        configSinks(backends, cfg);
    }

    private void configSinks(Set<Sink> availableSinks, Config cfg) {
        Set<String> enabledSinks = cfg.getSinks();
        for(Sink be : availableSinks) {
            if (enabledSinks.contains(be.id())) {
                backends.add(be);
            }
        }
    }

    public void start() {
        scheduledWork = scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                flush();
            }
        }, period, period, TimeUnit.SECONDS);
    }

    public void stop() {
        if (scheduledWork != null) {
            scheduledWork.cancel(true);
            scheduledWork = null;
        }
    }

    private void flush() {
        Exchanger<Snapshot> x = new Exchanger<>();
        FlusherEntry f = new FlusherEntry(x);
        while (!queue.offer(f)) {
            LockSupport.parkNanos(50000);
        }
        try {
            Snapshot s = x.exchange(null);
            for(Sink b : backends) {
                try {
                    b.accept(s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException e) {
            LOG.debug("Flusher interrupted", e);
        }
    }

    @Override
    public void report(PrintStream s) {
        s.append("=> Flusher (status: ").append(scheduledWork != null ? "running" : "stopped")
         .append(", queue: ").append(String.valueOf(queue.size())).append(")\n");
        for(Sink be : backends) {
            be.report(s);
        }
    }
}
