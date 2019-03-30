package io.btrace.jstatsd.server.api;

import java.io.PrintStream;

public interface StatsReporter {
    void report(PrintStream s);
}
