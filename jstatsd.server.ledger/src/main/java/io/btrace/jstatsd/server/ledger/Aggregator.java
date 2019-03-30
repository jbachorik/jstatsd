package io.btrace.jstatsd.server.ledger;

import io.btrace.jstatsd.server.api.StatsReporter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.PrintStream;

/**
 * A service wrapper for the ledger aggregation
 */
@Singleton
public final class Aggregator implements StatsReporter {
    private final LedgerInspector inspector;
    private final Flusher flusher;

    @Inject
    Aggregator(LedgerInspector i, Flusher f) {
        this.inspector = i;
        this.flusher = f;
    }

    public void start() {
        inspector.start();
        flusher.start();
    }

    public void stop() {
        flusher.stop();
        inspector.stop();
    }

    @Override
    public void report(PrintStream s) {
        inspector.report(s);
        flusher.report(s);
    }
}
