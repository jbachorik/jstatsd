package io.btrace.jstatsd.server.api.metrics;

import io.btrace.jstatsd.server.api.Ledger;
import io.btrace.jstatsd.server.api.Token;

import java.util.Map;

public class Histo extends Metric {
    public final long avg;
    public final long max;
    public final long min;
    public final long median;
    public final long percentile95;
    public final long percentile99;

    public Histo(Token key, Map<Token, Token> tags, long value) {
        this(new Identifier(key, tags), value);
    }

    public Histo(Identifier key, long value) {
        this(key, value, value, value, value, value, value);
    }

    public Histo(Identifier key, long avg, long max, long min, long median, long percentile95, long percentile99) {
        super(key);
        this.avg = avg;
        this.max = max;
        this.min = min;
        this.median = median;
        this.percentile95 = percentile95;
        this.percentile99 = percentile99;
    }

    @Override
    public void record(Ledger l) {
        l.add(this);
    }

    @Override
    public String toString() {
        return "Histo{" + "id=" + getId() + ", avg=" + avg + ", max=" + max + ", min=" + min + ", median=" + median + ", percentile95=" + percentile95 + ", percentile99=" + percentile99 + '}';
    }

}
