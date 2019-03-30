package io.btrace.jstatsd.server.parser;

import io.btrace.jstatsd.server.api.Token;
import io.btrace.jstatsd.server.api.metrics.Event;

final class EventFactory {
    static Event from(ByteArrayParser p) {
        try {
            p.mark();
            Token h = p.nextToken('{');
            if (h == null) {
                p.reset();
                return null;
            } else {
                if (!("_e".equals(h.asString()))) {
                    p.reset();
                    return null;
                }
            }
            return new Event();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return null;
        }
    }
}
