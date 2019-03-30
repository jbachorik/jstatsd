package io.btrace.jstatsd.server.ledger;

import io.btrace.jstatsd.server.api.Ledger;
import io.btrace.jstatsd.server.api.LedgerEntry;
import io.btrace.jstatsd.server.api.StatsReporter;
import io.btrace.jstatsd.server.api.execution.DaemonThreadFactory;
import io.btrace.jstatsd.server.api.metrics.Metric;
import io.btrace.jstatsd.server.queues.ProgressiveWaitStrategy;
import org.jctools.queues.MessagePassingQueue;

import javax.inject.Inject;
import java.io.PrintStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/**
 * LedgerInspector will consume the associated {@linkplain LedgerQueue}
 * and transfer the recorder metrics to {@link Ledger}.
 */
final class LedgerInspector implements MessagePassingQueue.Consumer<LedgerEntry>, StatsReporter {
    private final AtomicLong counter = new AtomicLong();
    private final AtomicLong startT = new AtomicLong(-1);
    private final AtomicLong endT = new AtomicLong(-1);

    private final ExecutorService worker = Executors.newSingleThreadExecutor(
        new DaemonThreadFactory("jStatsD Ledger Inspector")
    );

    private final Ledger reg;
    private final LedgerQueue queue;
    private final ProgressiveWaitStrategy wStrategy;

    private volatile Future<Void> inspectorTask = null;

    @Inject
    LedgerInspector(LedgerQueue lq, Ledger reg, ProgressiveWaitStrategy st) {
        this.reg = reg;
        this.queue = lq;
        this.wStrategy = st;
    }

    @Override
    public void accept(LedgerEntry e) {
        if (e instanceof Metric) {
            startT.compareAndSet(-1, System.nanoTime());
        }
        try {
            e.record(reg);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (e instanceof Metric) {
            counter.incrementAndGet();
            endT.set(System.nanoTime());
        }
    }

    public void start() {
        inspectorTask = worker.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                queue.drain(LedgerInspector.this, wStrategy, wStrategy.getXCondition());
                return null;
            }
        });
    }

    public void stop() {
        if (inspectorTask != null) {
            inspectorTask.cancel(true);
            inspectorTask = null;
        }
    }

    @Override
    public void report(PrintStream s) {
        long amnt = counter.get();
        long dur = (endT.get() - startT.get()) / 1000000;
        s.append("=> LedgerInspector (status: ").append(inspectorTask != null ? "running" : "stopped")
         .append(", queue: ").append(String.valueOf(queue.size()))
         .append(", messages: ").append(String.valueOf(amnt)).append(", duration: ").append(String.valueOf(dur)).append("ms")
         .append(", rate: ").append(String.valueOf((amnt > 0) ? (amnt / dur) : 0)).append(" msgs/ms)\n");
    }
}
