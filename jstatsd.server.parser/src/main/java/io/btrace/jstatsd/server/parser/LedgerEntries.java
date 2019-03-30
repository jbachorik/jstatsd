package io.btrace.jstatsd.server.parser;

import io.btrace.jstatsd.server.api.LedgerEntry;

import java.util.Iterator;

public final class LedgerEntries implements Iterable<LedgerEntry>{
    private static class IteratorImpl implements Iterator<LedgerEntry> {
        private final LedgerEntryParser p;
        private boolean reachedEnd = false;
        private LedgerEntry current = null;

        public IteratorImpl(LedgerEntryParser p) {
            this.p = p;
        }

        @Override
        public boolean hasNext() {
            if (current == null) {
                if (!reachedEnd) {
                    current = p.nextMessage();
                    reachedEnd = (current == null);
                }
            }
            return current != null;
        }

        @Override
        public LedgerEntry next() {
            LedgerEntry msg = null;
            if (current == null && !reachedEnd) {
                msg = p.nextMessage();
            } else {
                msg = current;
                current = null;
            }
            if (msg == null) {
                reachedEnd = true;
            }
            return msg;
        }
    }

    private final byte[] data;

    public LedgerEntries(byte[] arr) {
        this.data = arr;
    }

    @Override
    public Iterator<LedgerEntry> iterator() {
        return new IteratorImpl(new LedgerEntryParser(data));
    }
}
