package io.btrace.jstatsd.server.receiver;

import io.btrace.jstatsd.server.api.Service;
import io.btrace.jstatsd.server.api.StatsReporter;
import io.btrace.jstatsd.server.api.execution.DaemonThreadFactory;

import javax.inject.Inject;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class MessageReceiver implements Service, StatsReporter {
    private final MessageReceiverTask task;
    private final ExecutorService incoming = Executors.newSingleThreadExecutor(
        new DaemonThreadFactory("jStatsD Message Receiver")
    );

    private volatile Future<Void> receiverTask = null;

    @Inject
    MessageReceiver(MessageReceiverTask task) {
        this.task = task;
    }

    @Override
    public void start() {
        receiverTask = incoming.submit(task);
    }

    @Override
    public void stop() {
        if (receiverTask != null) {
            receiverTask.cancel(false);
            receiverTask = null;
        }
    }

    @Override
    public void report(PrintStream s) {
        s.append("=> MessageReceiver (status: ");
        s.append(receiverTask == null ? "stopped" : "running");
        s.append(", packets: ").append(String.valueOf(task.getReceivedPackets()));
        s.append(")\n");
    }
}
