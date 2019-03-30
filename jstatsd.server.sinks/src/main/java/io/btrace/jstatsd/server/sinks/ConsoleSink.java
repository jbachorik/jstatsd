package io.btrace.jstatsd.server.sinks;

import io.btrace.jstatsd.server.api.Snapshot;
import io.btrace.jstatsd.server.spi.Sink;

import java.io.PrintStream;

public class ConsoleSink extends Sink {
    @Override
    public String id() {
        return "console";
    }

    @Override
    public void accept(Snapshot s) {
        System.out.println(":: " + s);
    }

    @Override
    public void report(PrintStream s) {
        s.append("=> Backend[").append(id()).append("]\n");
    }
}
