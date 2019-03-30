package io.btrace.jstatsd.server.parser;

import io.btrace.jstatsd.server.api.Token;

import java.util.HashMap;
import java.util.Map;

abstract class StatsdMessage {
    private final ByteArrayParser p;
    protected final Map<Token, Token> tags = new HashMap<>();

    protected StatsdMessage(ByteArrayParser p) {
        this.p = p;
    }

    public Map<Token, Token> getTags() {
        return tags;
    }

    protected final Token requireNextToken(char delim) throws IllegalStateException {
        return requireNextToken(delim, false);
    }

    protected final Token requireNextTokenOrEnd(char delim) throws IllegalStateException {
        return requireNextToken(delim, true);
    }

    private Token requireNextToken(char delim, boolean mayStop) throws IllegalStateException {
        Token val = p.nextToken(delim, mayStop);
        if (val == null) {
            throw new IllegalStateException();
        }
        return val;
    }

    protected final Token tryNextToken(char delim) {
        return tryNextToken(delim, false);
    }

    protected final Token tryNextTokenOrEnd(char delim) {
        return tryNextToken(delim, true);
    }

    private Token tryNextToken(char delim, boolean mayStop) {
        p.mark();
        Token val = p.nextToken(delim, mayStop);
        if (val == null) {
            p.reset();
        }
        return val;
    }

    protected final void readTags() {
        if (tryNextToken('#') != null) {
            boolean hasMoreTags = true;
            do {
                Token value = Token.EMPTY;
                Token key = tryNextToken(':');
                if (key == null) {
                    key = tryNextToken(',');
                    if (key == null) {
                        key = requireNextTokenOrEnd(',');
                        hasMoreTags = false;
                    }
                } else {
                    value = tryNextToken(',');
                    if (value == null) {
                        value = requireNextTokenOrEnd(',');
                        hasMoreTags = false;
                    }
                }
                tags.put(key, value);
            } while (hasMoreTags);
        }
    }
}
