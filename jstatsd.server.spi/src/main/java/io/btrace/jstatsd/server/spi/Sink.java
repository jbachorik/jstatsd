package io.btrace.jstatsd.server.spi;

import io.btrace.jstatsd.server.api.Snapshot;
import io.btrace.jstatsd.server.api.StatsReporter;

public abstract class Sink implements StatsReporter {
    public abstract String id();
    public abstract void accept(Snapshot s);
}
