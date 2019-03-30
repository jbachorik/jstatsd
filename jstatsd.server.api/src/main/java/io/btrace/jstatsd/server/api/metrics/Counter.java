package io.btrace.jstatsd.server.api.metrics;

import io.btrace.jstatsd.server.api.Ledger;
import io.btrace.jstatsd.server.api.Token;

import java.util.Map;

public final class Counter extends Metric {
    public final long count;

    public Counter(Token key, Map<Token, Token> tags, long count) {
        this(new Identifier(key, tags), count);
    }

    public Counter(Identifier key, long count) {
        super(key);
        this.count = count;
    }

    @Override
    public void record(Ledger l) {
        l.add(this);
    }

    @Override
    public String toString() {
        return "Counter{" + "id=" + getId() + ", count=" + count + '}';
    }
}
