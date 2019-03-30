package io.btrace.jstatsd.server.parser;

import io.btrace.jstatsd.server.api.metrics.Event;

final class StatsdEvent extends StatsdMessage {
    enum Priority {
        NORMAL, LOW
    }
    enum AlertType {
        INFO, WARNING, ERROR, SUCCESS
    }

    private final int titleLen;
    private final int textLen;
    private final String title;
    private final String text;
    private long timestamp = -1L;
    private String host = null;
    private String group = null;
    private String sourceType = null;
    private Priority priority;
    private AlertType alertType;

    StatsdEvent(ByteArrayParser p) throws IllegalStateException {
        super(p);

        titleLen = requireNextToken(',').asInt();
        textLen = requireNextToken('}').asInt();
        requireNextToken(':');
        title = requireNextToken('|').asString();
        text = requireNextToken('|').asString();

        while (true) {
            readSpecs();
            readTags();
        }
    }

    private void readSpecs() {
        OUTER:
        while (true) {
            String selector = tryNextTokenOrEnd(':').asString();
            int len = selector.trim().length();
            switch (len) {
                case 1:
                    String value = requireNextTokenOrEnd('|').asString();
                    switch (selector.charAt(0)) {
                        case 'd': {
                            timestamp = Long.parseLong(value);
                            break;
                        }
                        case 'h': {
                            host = value;
                            break;
                        }
                        case 'k': {
                            group = value;
                            break;
                        }
                        case 's': {
                            sourceType = value;
                            break;
                        }
                        case 'p': {
                            priority = Priority.valueOf(value);
                            break;
                        }
                        case 't': {
                            alertType = AlertType.valueOf(value);
                            break;
                        }
                        default: {
                            System.err.println("Unknown event property selector '" + selector + "'");
                        }
                    }   break;
                case 0:
                    // reached the end
                    break OUTER;
                default:
                    throw new IllegalStateException();
            }
        }
    }

    protected Event toEvent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String toString() {
        return "StatsdEvent{titleLen=" + titleLen + ", textLen=" + textLen + ", title=" + title + ", text=" + text + ", timestamp=" + timestamp + ", host=" + host + ", group=" + group + ", sourceType=" + sourceType + ", priority=" + priority + ", alertType=" + alertType + ", tags=" + tags + '}';
    }
}
