package io.btrace.jstatsd.server;

import java.io.IOException;

/**
 * A single jStatsD server instance
 */
public final class ServerInstance {
    private final ServerImpl impl;

    ServerInstance(ServerImpl impl) {
        this.impl = impl;
    }

    /**
     * Start the jStatsD server
     * @throws IOException
     */
    public void start() throws IOException {
        impl.start();
    }

    /**
     * Stop the jStatsD server
     * @throws IOException
     */
    public void stop() throws IOException {
        impl.stop();
    }
}
