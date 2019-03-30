package io.btrace.jstatsd.server.receiver;

import io.btrace.jstatsd.server.api.Config;
import io.btrace.jstatsd.server.api.StatsReporter;
import io.btrace.jstatsd.server.api.execution.DaemonThreadFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * A service wrapper for concurrently running {@link BytePacketProcessor} instances.
 */
public final class BytePacketService implements StatsReporter {
    private final Provider<BytePacketProcessor> provider;
    private final Collection<BytePacketProcessor> activeWorkers = new LinkedList<>();
    private final Collection<Future<?>> workerTasks = new LinkedList<>();
    private final int workerCap;
    private final ExecutorService queueService;

    @Inject
    BytePacketService(Provider<BytePacketProcessor> p, Config cfg) {
        this.provider = p;
        this.workerCap = Math.min(cfg.getConcurrencyLevel(), Runtime.getRuntime().availableProcessors() - 2);
        this.queueService = Executors.newFixedThreadPool(
            workerCap, new DaemonThreadFactory("jStatsD BytePacket Parser", true)
        );
    }

    public void start() {
        for(int i = 0; i < workerCap; i++) {
            BytePacketProcessor qw = provider.get();
            activeWorkers.add(qw);
            workerTasks.add(queueService.submit(qw));
        }
    }

    public void stop() {
        for(BytePacketProcessor qw : activeWorkers) {
            qw.onStop();
        }
        for(Future f : workerTasks) {
            f.cancel(false);
        }
        activeWorkers.clear();
        workerTasks.clear();
    }

    @Override
    public void report(PrintStream s) {
        s.append("=> BytePacketService (status: ").append(workerTasks.isEmpty() ? "stopped" : "running")
         .append(", concurrency: ").append(String.valueOf(workerCap)).append(")\n");
    }
}
