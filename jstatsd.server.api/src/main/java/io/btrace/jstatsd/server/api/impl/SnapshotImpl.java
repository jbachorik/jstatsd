package io.btrace.jstatsd.server.api.impl;

import io.btrace.jstatsd.server.api.metrics.Counter;
import io.btrace.jstatsd.server.api.metrics.Event;
import io.btrace.jstatsd.server.api.metrics.Gauge;
import io.btrace.jstatsd.server.api.metrics.Histo;

import java.util.List;

public interface SnapshotImpl {
    boolean isEmpty();
    List<Counter> getCounters();

    List<Gauge> getGauges();

    List<Histo> getSeries();

    List<Event> getEvents();

    List<Counter> getUniques();
}
