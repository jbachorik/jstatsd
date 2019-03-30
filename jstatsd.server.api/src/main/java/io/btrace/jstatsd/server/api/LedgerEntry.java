package io.btrace.jstatsd.server.api;

public interface LedgerEntry {
    void record(Ledger l);
}
