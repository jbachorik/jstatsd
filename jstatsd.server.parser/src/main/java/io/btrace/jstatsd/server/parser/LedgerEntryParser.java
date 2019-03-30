package io.btrace.jstatsd.server.parser;

import io.btrace.jstatsd.server.api.LedgerEntry;

final class LedgerEntryParser {
    private static final char[] CONTROL_CHARS = ByteArrayParser.delimiters("|:#@=,");
    private final ByteArrayParser p;

    LedgerEntryParser(byte[] data) {
        this.p = new ByteArrayParser(data, CONTROL_CHARS);
    }

    LedgerEntry nextMessage() {
        LedgerEntry e = EventFactory.from(p);
        if (e == null) {
            e = MetricFactory.from(p);
        }
        if (e == null) {
            // message is corrupted; skip to the next message if it exists
            p.nextToken(ByteArrayParser.EOM);
        }
        return e;
    }

    void rewind() {
        p.rewind();
    }
}
