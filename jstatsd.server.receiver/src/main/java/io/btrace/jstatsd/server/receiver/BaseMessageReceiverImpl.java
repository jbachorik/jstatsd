package io.btrace.jstatsd.server.receiver;

import io.btrace.jstatsd.server.api.Config;

public abstract class BaseMessageReceiverImpl implements MessageReceiverImpl {
    protected final int port;
    protected final Config cfg;
    protected final BytePacketQueue transferQueue;

    protected BaseMessageReceiverImpl(int port, BytePacketQueue tq, Config conf) {
        this.port = port;
        this.cfg = conf;
        this.transferQueue = tq;
    }
}
