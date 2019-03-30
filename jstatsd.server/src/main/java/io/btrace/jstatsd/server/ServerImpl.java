package io.btrace.jstatsd.server;

import io.btrace.jstatsd.server.api.StatsReporter;
import io.btrace.jstatsd.server.ledger.Aggregator;
import io.btrace.jstatsd.server.receiver.BytePacketService;
import io.btrace.jstatsd.server.receiver.MessageReceiver;

import javax.inject.Inject;
import java.io.IOException;
import java.io.PrintStream;

class ServerImpl implements StatsReporter {
    private final MessageReceiver receiver;
    private final BytePacketService packetService;
    private final Aggregator aggregator;

    private volatile boolean running = false;

    @Inject
    ServerImpl(MessageReceiver receiver, BytePacketService packetService, Aggregator aggr) {
        this.receiver = receiver;
        this.packetService = packetService;
        this.aggregator = aggr;
    }

    public void start() throws IOException {
        aggregator.start();
        packetService.start();
        receiver.start();
        running = true;

        report(System.out);
    }

    public void stop() {
        receiver.stop();
        packetService.stop();
        aggregator.stop();
        running = false;

        report(System.out);
    }

    @Override
    public void report(PrintStream s) {
        s.append("=============================================\n");
        s.append("=> jStatsD Server (status = ").append(running ? "running" : "stopped").append(")\n");
        receiver.report(s);
        packetService.report(s);
        aggregator.report(s);
        s.append("=============================================\n");
    }
}
