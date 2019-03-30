package io.btrace.jstatsd.server.ledger;

import com.google.inject.AbstractModule;
import io.btrace.jstatsd.server.api.Ledger;

public final class LedgerModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Ledger.class).to(LedgerImpl.class);
    }
}
