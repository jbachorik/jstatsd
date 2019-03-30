package io.btrace.jstatsd.server.ledger;

import io.btrace.jstatsd.server.api.Ledger;
import io.btrace.jstatsd.server.api.LedgerEntry;
import io.btrace.jstatsd.server.api.Snapshot;

import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class FlusherEntry implements LedgerEntry {

    private final Exchanger<Snapshot> rslt;

    public FlusherEntry(Exchanger<Snapshot> ret) {
        this.rslt = ret;
    }

    @Override
    public void record(Ledger r) {
        try {
            rslt.exchange(r.snapshot(), 200, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | TimeoutException e) {
            System.err.println("Failed to post back the metrics snapshot. " + e.getMessage());
        }
    }
}
