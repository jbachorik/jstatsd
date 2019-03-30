package io.btrace.jstatsd.server.receiver.udp;

import io.btrace.jstatsd.server.api.Config;
import io.btrace.jstatsd.server.receiver.BaseMessageReceiverImpl;
import io.btrace.jstatsd.server.receiver.BytePacketQueue;

import javax.inject.Inject;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

public class UdpAsyncReceiver extends BaseMessageReceiverImpl {
    private static final int BUFFER_SIZE = 512;

    private Selector selector;

    @Inject
    UdpAsyncReceiver(BytePacketQueue tq, Config cfg) {
        super(cfg.getPort(), tq, cfg);
    }

    @Override
    public boolean isEnabled() {
        return cfg.getMode().equalsIgnoreCase("udp") && cfg.isAsync();
    }

    @Override
    public void setup() throws IOException {
        ByteBuffer bb = ByteBuffer.allocateDirect(BUFFER_SIZE);
        selector = Selector.open();
        DatagramChannel channel = DatagramChannel.open();
        InetSocketAddress isa = new InetSocketAddress(port);
        channel.socket().bind(isa);
        channel.configureBlocking(false);
        SelectionKey clientKey = channel.register(selector, SelectionKey.OP_READ);
        clientKey.attach(bb);
    }

    @Override
    public int receiveMessage() {
        int count = 0;
        try {
            selector.select();
            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
            while (selectedKeys.hasNext()) {
                try {
                    SelectionKey key = selectedKeys.next();
                    selectedKeys.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isReadable()) {
                        transferQueue.offer(readFromSelection(key));
                        count++;
                    }
                } catch (IOException e) {
                    System.err.println("Error processing selection key: " +(e.getMessage()!=null?e.getMessage():""));
                }
            }
        } catch (IOException e) {
            System.err.println("Error accessing selector: " +(e.getMessage()!=null?e.getMessage():""));
        }
        return count;
    }

    private byte[] readFromSelection(SelectionKey k) throws IOException {
        DatagramChannel ch = (DatagramChannel)k.channel();
        ByteBuffer bb = (ByteBuffer)k.attachment();
        bb.rewind();
        ch.receive(bb);
        byte[] arr = new byte[bb.position()];
        bb.rewind();
        bb.get(arr);
        return arr;
    }
}
