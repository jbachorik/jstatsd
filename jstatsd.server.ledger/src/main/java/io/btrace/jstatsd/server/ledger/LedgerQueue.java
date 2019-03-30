package io.btrace.jstatsd.server.ledger;

import io.btrace.jstatsd.server.api.LedgerEntry;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpscArrayQueue;

import javax.inject.Singleton;

@Singleton
public final class LedgerQueue implements MessagePassingQueue<LedgerEntry>{
    private final MessagePassingQueue<LedgerEntry> delegate = new MpscArrayQueue<>(65536);

    @Override
    public boolean offer(LedgerEntry e) {
        return delegate.offer(e);
    }

    @Override
    public LedgerEntry poll() {
        return delegate.poll();
    }

    @Override
    public LedgerEntry peek() {
        return delegate.peek();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public int capacity() {
        return delegate.capacity();
    }

    @Override
    public boolean relaxedOffer(LedgerEntry e) {
        return delegate.relaxedOffer(e);
    }

    @Override
    public LedgerEntry relaxedPoll() {
        return delegate.relaxedPoll();
    }

    @Override
    public LedgerEntry relaxedPeek() {
        return delegate.relaxedPeek();
    }

    @Override
    public int drain(Consumer<LedgerEntry> c) {
        return delegate.drain(c);
    }

    @Override
    public int fill(Supplier<LedgerEntry> s) {
        return delegate.fill(s);
    }

    @Override
    public int drain(Consumer<LedgerEntry> c, int limit) {
        return delegate.drain(c, limit);
    }

    @Override
    public int fill(Supplier<LedgerEntry> s, int limit) {
        return delegate.fill(s, limit);
    }

    @Override
    public void drain(Consumer<LedgerEntry> c, WaitStrategy wait, ExitCondition exit) {
        delegate.drain(c, wait, exit);
    }

    @Override
    public void fill(Supplier<LedgerEntry> s, WaitStrategy wait, ExitCondition exit) {
        delegate.fill(s, wait, exit);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
