package io.btrace.jstatsd.server;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.btrace.jstatsd.server.api.Config;
import io.btrace.jstatsd.server.ledger.LedgerModule;
import io.btrace.jstatsd.server.receiver.ReceiverModule;
import io.btrace.jstatsd.server.sinks.SinksModule;

import javax.inject.Singleton;
import java.util.Properties;

public class MainModule extends AbstractModule {
    private final Properties cfgProps;

    public MainModule(Properties cfgProps) {
        this.cfgProps = cfgProps;
    }

    public MainModule() {
        this.cfgProps = null;
    }

    @Override
    protected void configure() {
        install(new LedgerModule());
        install(new ReceiverModule());
        install(new SinksModule());
    }

    @Provides
    @Singleton
    public Config getConfig() {
        return new Config(cfgProps);
    }
}
