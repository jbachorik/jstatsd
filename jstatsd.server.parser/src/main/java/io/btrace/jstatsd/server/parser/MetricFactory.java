package io.btrace.jstatsd.server.parser;

import io.btrace.jstatsd.server.api.Token;
import io.btrace.jstatsd.server.api.metrics.Metric;
import org.apache.log4j.Logger;

final class MetricFactory {
    private static final Logger LOG = Logger.getLogger(MetricFactory.class);

    static Metric from(ByteArrayParser p) {
        p.mark();
        try {
            StatsdMetric m = new StatsdMetric(p);
            if (m.isValid()) {
                return m.toMetric();
            }
        } catch (IllegalStateException e) {
            if (LOG.isDebugEnabled()) {
                p.rewind();
                Token err = p.nextToken(ByteArrayParser.EOM, true);
                LOG.debug("Error parsing message: '" + err.asString() + "'. Dropping message.");
            } else {
                p.nextToken(ByteArrayParser.EOM, true);
            }
        }
        return null;
    }

}
