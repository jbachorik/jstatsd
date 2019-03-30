package io.btrace.jstatsd.server.receiver;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import io.btrace.jstatsd.server.receiver.udp.UdpAsyncReceiver;
import io.btrace.jstatsd.server.receiver.udp.UdpSyncReceiver;

public final class ReceiverModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder<MessageReceiverImpl> uriBinder = Multibinder.newSetBinder(binder(), MessageReceiverImpl.class);
        uriBinder.addBinding().to(UdpAsyncReceiver.class);
        uriBinder.addBinding().to(UdpSyncReceiver.class);
    }
}
