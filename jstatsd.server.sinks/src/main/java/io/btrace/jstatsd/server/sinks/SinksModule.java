package io.btrace.jstatsd.server.sinks;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import io.btrace.jstatsd.server.spi.Sink;

public final class SinksModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder<Sink> uriBinder = Multibinder.newSetBinder(binder(), Sink.class);
        uriBinder.addBinding().to(ConsoleSink.class);
        uriBinder.addBinding().to(ElasticSearchSink.class);
    }
}
