package io.btrace.jstatsd.server.api;

import io.btrace.jstatsd.server.api.metrics.*;

public abstract class Ledger {
    /**
     * Increment the counter value
     * @param c {@linkplain Counter} metric instance
     */
    public abstract void add(Counter c);

    /**
     * Adjust (increase/decrease) the gauge value
     * @param g {@linkplain Gauge} metric instance
     */
    public abstract void add(Gauge g);

    /**
     * Set the gauge value
     * @param g {@linkplain Gauge} metric instance
     */
    public abstract void set(Gauge g);

    /**
     * Record a new value for statistically analyzed metric
     * @param a {@linkplain Histo} metric instance
     */
    public abstract void add(Histo a);

    public abstract void add(Event e);

    public abstract void add(Unique u);

    public abstract Snapshot snapshot();
}
