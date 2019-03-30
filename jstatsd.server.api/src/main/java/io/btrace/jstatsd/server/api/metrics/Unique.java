package io.btrace.jstatsd.server.api.metrics;

import io.btrace.jstatsd.server.api.Ledger;
import io.btrace.jstatsd.server.api.Token;

import java.util.Map;

public final class Unique extends Metric {
    private final Token val;

    public Unique(Token key, Map<Token, Token> tags, Token val) {
        this(new Identifier(key, tags), val);
    }

    public Unique(Identifier key, Token val) {
        super(key);
        this.val = val;
    }

    public Token getValue() {
        return val;
    }

    @Override
    public void record(Ledger l) {
        l.add(this);
    }

    @Override
    public String toString() {
        return "Unique{" + "id=" + getId() + ", val=" + val + '}';
    }
}
