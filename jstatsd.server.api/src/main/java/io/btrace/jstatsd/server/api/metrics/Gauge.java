package io.btrace.jstatsd.server.api.metrics;

import io.btrace.jstatsd.server.api.Ledger;
import io.btrace.jstatsd.server.api.Token;

import java.util.Map;

public final class Gauge extends Metric {
    public final long value;
    private final int operation;

    public Gauge(Token key, Map<Token, Token> tags, long value, int operation) {
        this(new Identifier(key, tags), value, operation);
    }

    public Gauge(Identifier key, long value, int operation) {
        super(key);
        this.value = value;
        this.operation = operation;
    }

    @Override
    public void record(Ledger l) {
        if (operation != 0) {
            l.add(this);
        } else {
            l.set(this);
        }
    }

    @Override
    public String toString() {
        return "Gauge{" + "id=" + getId() + ", value=" + value + ", operation=" + operation + '}';
    }
}
