package io.btrace.jstatsd.server.api;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * StatsD server configuration.
 * One instance will be shared between all the interested parties.
 */
@Singleton
public final class Config {
    public static final String AGGREGATOR_FLUSH_PERIOD_KEY = "aggregator.flush.period";
    public static final String AGGREGATOR_SINKS_KEY = "aggregator.sinks";
    public static final String MAX_CONC_LEVEL_KEY = "queue.workers.cap";
    public static final String RECEIVER_PORT_KEY = "receiver.port";
    public static final String RECEIVER_ASYNC_KEY = "receiver.async";
    public static final String RECEIVER_MODE_KEY = "receiver.mode";

    private final Properties props = new Properties();

    public Config(Properties props) {
        try {
            this.props.load(Config.class.getResourceAsStream("config.properties"));
            if (props != null) {
                this.props.putAll(props);
            }
        } catch (IOException e) {
            // missing required file; nothing else to to but fail
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @return port the server will run on
     */
    public int getPort() {
        return getInt(RECEIVER_PORT_KEY, 8125);
    }

    /**
     *
     * @return concurrency level for processing the network packets
     */
    public int getConcurrencyLevel() {
        return getInt(MAX_CONC_LEVEL_KEY, 2);
    }

    /**
     *
     * @return the aggregator flush period in seconds
     */
    public int getFlushPeriod() {
        return getInt(AGGREGATOR_FLUSH_PERIOD_KEY, 10);
    }

    /**
     *
     * @return async i/o is to be used
     */
    public boolean isAsync() {
        return getBoolean(RECEIVER_ASYNC_KEY, true);
    }

    /**
     *
     * @return either 'udp' or 'tcp'
     */
    public String getMode() {
        return getString(RECEIVER_MODE_KEY, "udp");
    }

    /**
     *
     * @return the set of all enabled backends identifiers
     */
    public Set<String> getSinks() {
        return getStrings(AGGREGATOR_SINKS_KEY);
    }

    /**
     *
     * @param sink the sink identifier
     * @param key the property key
     * @return the key to locate the sink property
     */
    public String getSinkPropKey(String sink, String key) {
        return AGGREGATOR_SINKS_KEY + "." + sink + "." + key;
    }

    /**
     *
     * @param key the property key
     * @param defval the default value
     * @return the property value or the default value
     * @throws NumberFormatException
     */
    public int getInt(String key, int defval) {
        String v = props.getProperty(key, String.valueOf(defval));
        return Integer.parseInt(v);
    }

    /**
     *
     * @param key the property key
     * @param defval the default value
     * @return the property value or the default value
     */
    public String getString(String key, String defval) {
        String v = props.getProperty(key, defval);
        return v;
    }

    /**
     * Will split a property value by "," to generate a set of unique values
     * @param key the property key
     * @return the set of unique values
     */
    public Set<String> getStrings(String key) {
        Set<String> set = new HashSet<>();
        String v = props.getProperty(key);
        if (v != null) {
            for(String s : v.split(",")) {
                set.add(s.toLowerCase().trim());
            }
        }
        return set;
    }

    /**
     *
     * @param key the property key
     * @param defval the default value
     * @return the property value of the default value
     */
    public boolean getBoolean(String key, boolean defval) {
        String v = props.getProperty(key, String.valueOf(defval));
        return Boolean.parseBoolean(v);
    }
}
