package io.btrace.jstatsd.server.receiver.udp;

import io.btrace.jstatsd.server.api.Config;
import io.btrace.jstatsd.server.receiver.BaseMessageReceiverImpl;
import io.btrace.jstatsd.server.receiver.BytePacketQueue;

import javax.inject.Inject;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

public class UdpSyncReceiver extends BaseMessageReceiverImpl {
    private static final int BUFFER_SIZE = 512;

    private DatagramSocket dgs;
    private DatagramPacket dgp;

    @Inject
    UdpSyncReceiver(BytePacketQueue tq, Config cfg) {
        super(cfg.getPort(), tq, cfg);
    }

    @Override
    public boolean isEnabled() {
        return cfg.getMode().equalsIgnoreCase("udp") && !cfg.isAsync();
    }

    @Override
    public void setup() throws IOException {
        byte[] buff = new byte[BUFFER_SIZE];
        dgs = new DatagramSocket(port);
        dgp = new DatagramPacket(buff, BUFFER_SIZE);
    }

    @Override
    public int receiveMessage() {
        try {
            dgs.receive(dgp);
            byte[] arr = Arrays.copyOfRange(dgp.getData(), dgp.getOffset(), dgp.getOffset() + dgp.getLength() - 1);
            transferQueue.offer(arr);
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }


}
