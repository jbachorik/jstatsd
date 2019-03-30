package io.btrace.jstatsd.server.api.metrics;

import io.btrace.jstatsd.server.api.Ledger;
import io.btrace.jstatsd.server.api.LedgerEntry;

public final class Event implements LedgerEntry {
    public Event() {
    }

    @Override
    public void record(Ledger l) {
        l.add(this);
    }

    @Override
    public String toString() {
        return "Event{}";
    }
}
