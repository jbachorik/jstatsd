package io.btrace.jstatsd.server.parser;

import io.btrace.jstatsd.server.api.Token;
import io.btrace.jstatsd.server.api.metrics.*;
import org.apache.log4j.Logger;

public final class StatsdMetric extends StatsdMessage {
    private final static Logger LOG = Logger.getLogger(ByteArrayParser.class);

    private final Token name;
    private final Token value;
    private final Token typeStr;
    private Token samplingStr;

    private double sampling = Double.NaN;

    StatsdMetric(ByteArrayParser p) throws IllegalStateException {
        super(p);
        name = tryNextToken(':');
        Token v = null;
        Token t = null;
        if (name != null && !name.isEmpty()) {
            v = tryNextToken('|');
            if (v != null) {
                Token qualifier = tryNextTokenOrEnd('|');
                if (qualifier != null) {
                    t = (!qualifier.isEmpty() && qualifier.length() <= 2) ? qualifier : null;
                    readSampling();
                    readTags();
                }
            }
        }
        value = v;
        typeStr = t;
    }

    double getSampling() {
        if (sampling != Double.NaN) {
            return sampling;
        }
        return sampling = (samplingStr != null ? Double.parseDouble(samplingStr.asString()) : 0d);
    }

    private void readSampling() {
        if (tryNextToken('@') != null) {
            samplingStr = requireNextTokenOrEnd('|');
        }
    }

    boolean isValid() {
        return name != null && value != null && !value.isEmpty() &&
               typeStr != null && !typeStr.isEmpty() &&
               (samplingStr == null || !samplingStr.isEmpty());
    }

    protected Metric toMetric() {
        switch (typeStr.asString()) {
            case "c": {
                long val = value.asLong();
                return new Counter(name, tags, val);
            }
            case "g": {
                int delta = value.getDeltaPrefix();
                long val = value.asLong();
                return new Gauge(name, tags, val, delta);
            }
            case "h":
            case "ms": {
                return new Histo(name, tags, value.asLong());
            }
            case "u":
            case "s": {
                return new Unique(name, tags, value);
            }
            default: {
                LOG.warn("Unknown metric type '" + typeStr.asString() + "'");
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "StatsdMetric{" + "name=" + name + ", value=" + value + ", type=" + typeStr + ", sampling=" + samplingStr + ", tags=" + tags + '}';
    }
}
