package io.btrace.jstatsd.server.receiver;

import java.io.IOException;

public interface MessageReceiverImpl {
    boolean isEnabled();
    void setup() throws IOException;
    int receiveMessage();
}
