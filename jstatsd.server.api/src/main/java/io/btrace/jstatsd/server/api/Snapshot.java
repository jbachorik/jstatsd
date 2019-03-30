package io.btrace.jstatsd.server.api;

import io.btrace.jstatsd.server.api.impl.SnapshotImpl;
import io.btrace.jstatsd.server.api.metrics.Counter;
import io.btrace.jstatsd.server.api.metrics.Event;
import io.btrace.jstatsd.server.api.metrics.Gauge;
import io.btrace.jstatsd.server.api.metrics.Histo;

import java.util.List;

public final class Snapshot {
    private final SnapshotImpl impl;

    public Snapshot(SnapshotImpl impl) {
        this.impl = impl;
    }

    public boolean isEmpty() {
        return impl.isEmpty();
    }

    public List<Counter> getCounters() {
        return impl.getCounters();
    }

    public List<Gauge> getGauges() {
        return impl.getGauges();
    }

    public List<Histo> getSeries() {
        return impl.getSeries();
    }

    public List<Event> getEvents() {
        return impl.getEvents();
    }

    public List<Counter> getUniques() {
        return impl.getUniques();
    }

    @Override
    public String toString() {
        return impl.toString();
    }
}
