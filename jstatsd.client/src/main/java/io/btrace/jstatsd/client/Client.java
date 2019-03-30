package io.btrace.jstatsd.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.*;

/**
 * A simple way to submit <a href="https://github.com/etsy/statsd/">statsd</a> metrics.
 *
 * @author Jaroslav Bachorik
 */
final public class Client {
    private static final Charset CHARSET = Charset.forName("ascii");
    public enum Priority {
        NORMAL, LOW
    }
    public enum AlertType {
        INFO, WARNING, ERROR, SUCCESS
    }

    private final static class Singleton {
        private final static Client INSTANCE = new Client();
    }

    private final BlockingQueue<String> q = new ArrayBlockingQueue<>(120000);
    private final ExecutorService e = Executors.newSingleThreadExecutor(
        new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "jStatsD Client Submitter");
                t.setDaemon(true);
                return t;
            }
        }
    );

    public static Client getInstance() {
        return Singleton.INSTANCE;
    }

    private Client() {
        e.submit(new Runnable() {
            @Override
            public void run() {
                DatagramSocket ds = null;
                try {
                    ds = new DatagramSocket();
                    DatagramPacket dp = new DatagramPacket(new byte[0], 0);
                    try {
                        dp.setAddress(InetAddress.getByName("localhost"));
                    } catch (UnknownHostException e) {
                        System.err.println("[statsd] invalid host defined: " + "localhost");
                        dp.setAddress(InetAddress.getLoopbackAddress());
                    } catch (SecurityException e) {
                        dp.setAddress(InetAddress.getLoopbackAddress());
                    }
                    dp.setPort(8125);

                    while (true) {
                        Collection<String> msgs = new LinkedList<>();
                        msgs.add(q.take());
                        q.drainTo(msgs);

                        StringBuilder sb = new StringBuilder();
                        for(String m : msgs) {
                            if (sb.length() + m.length() < 511) {
                                sb.append(m).append('\n');
                            } else {
                                dp.setData(sb.toString().getBytes(CHARSET));
                                ds.send(dp);
                                sb.setLength(0);
                            }
                        }
                        if (sb.length() > 0) {
                            dp.setData(sb.toString().getBytes(CHARSET));
                            ds.send(dp);
                        }
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Increase the given counter by 1
     * @param name the counter name
     */
    public void increment(String name) {
        delta(name, 1, 0.0d, null);
    }

    /**
     * Increase the given counter by 1
     * @param name the counter name
     * @param tags
     *     Only for DogStatsD compatible collectors.
     *     Assigned comma delimited tags. A tag value is delimited by colon.
     */
    public void increment(String name, String tags) {
        delta(name, 1, 0.0d, tags);
    }

    /**
     * Increase the given counter by 1
     * @param name the counter name
     * @param sampleRate
     *     the sampling rate being employed. For example, a rate of 0.1 would
     *     tell StatsD that this counter is being sent
     *     sampled every 1/10th of the time.
     */
    public void increment(String name, double sampleRate) {
        delta(name, 1, sampleRate, null);
    }

    /**
     * Increase the given counter by 1
     * @param name the counter name
     * @param sampleRate
     *     the sampling rate being employed. For example, a rate of 0.1 would
     *     tell StatsD that this counter is being sent
     *     sampled every 1/10th of the time.
     * @param tags
     *     Only for DogStatsD compatible collectors.
     *     Assigned comma delimited tags. A tag value is delimited by colon.
     */
    public void increment(String name, double sampleRate, String tags) {
        delta(name, 1, sampleRate, tags);
    }

    /**
     * Decrease the given counter by 1
     * @param name the counter name
     */
    public void decrement(String name) {
        delta(name, -1, 0.0d, null);
    }

    /**
     * Decrease the given counter by 1
     * @param name the counter name
     * @param tags
     *     Only for DogStatsD compatible collectors.
     *     Assigned comma delimited tags. A tag value is delimited by colon.
     */
    public void decrement(String name, String tags) {
        delta(name, -1, 0.0d, tags);
    }

    /**
     * Decrease the given counter by 1
     * @param name the counter name
     * @param sampleRate
     *     the sampling rate being employed. For example, a rate of 0.1 would
     *     tell StatsD that this counter is being sent
     *     sampled every 1/10th of the time.
     */
    public void decrement(String name, double sampleRate) {
        delta(name, -1, sampleRate, null);
    }

    /**
     * Decrease the given counter by 1
     * @param name the counter name
     * @param sampleRate
     *     the sampling rate being employed. For example, a rate of 0.1 would
     *     tell StatsD that this counter is being sent
     *     sampled every 1/10th of the time.
     * @param tags
     *     Only for DogStatsD compatible collectors.
     *     Assigned comma delimited tags. A tag value is delimited by colon.
     */
    public void decrement(String name, double sampleRate, String tags) {
        delta(name, -1, sampleRate, tags);
    }

    /**
     * Adjusts the specified counter by a given delta.
     *
     * @param name
     *     the name of the counter to adjust
     * @param count
     *     the counter value
     */
    public void count(String name, long count) {
        count(name, count, null);
    }

    /**
     * Adjusts the specified counter by a given delta.
     *
     * @param name
     *     the name of the counter to adjust
     * @param count
     *     the counter value
     * @param tags
     *     Only for DogStatsD compatible collectors.
     *     Assigned comma delimited tags. A tag value is delimited by colon.
     */
    public void count(String name, long count, String tags) {
        count(name, count, 0d, tags);
    }

    /**
     * Adjusts the specified counter by a given delta.
     *
     * @param name
     *     the name of the counter to adjust
     * @param count
     *     the counter value
     * @param sampleRate
     *     the sampling rate being employed. For example, a rate of 0.1 would
     *     tell StatsD that this counter is being sent
     *     sampled every 1/10th of the time.
     */
    public void count(String name, long count, double sampleRate) {
        count(name, count, sampleRate, null);
    }

    /**
     * Adjusts the specified counter by a given delta.
     *
     * @param name
     *     the name of the counter to adjust
     * @param count
     *     the counter value
     * @param sampleRate
     *     the sampling rate being employed. For example, a rate of 0.1 would
     *     tell StatsD that this counter is being sent
     *     sampled every 1/10th of the time.
     * @param tags
     *     Only for DogStatsD compatible collectors.
     *     Assigned comma delimited tags. A tag value is delimited by colon.
     */
    public void count(String name, long count, double sampleRate, String tags) {
        submit(name, count, sampleRate, "c", tags);
    }

    /**
     * Sets the specified gauge to a given value.
     *
     * @param name
     *     the name of the gauge to set
     * @param value
     *     the value to set the gauge to
     */
    public void gauge(String name, long value) {
        gauge(name, value, null);
    }

    /**
     * Sets the specified gauge to a given value.
     *
     * @param name
     *     the name of the gauge to set
     * @param value
     *     the value to set the gauge to
     * @param tags
     *     Only for DogStatsD compatible collectors.
     *     Assigned comma delimited tags. A tag value is delimited by colon.
     */
    public void gauge(String name, long value, String tags) {
        submit(name, value, 0d, "g", tags);
    }

    /**
     * Records the timing information for the specified metric.
     *
     * @param name
     *     the metric name
     * @param value
     *     the measured time
     */
    public void time(String name, long value) {
        time(name, value, null);
    }

    /**
     * Records the timing information for the specified metric.
     *
     * @param name
     *     the metric name
     * @param value
     *     the measured time
     * @param sampleRate
     *     the sampling rate being employed. For example, a rate of 0.1 would
     *     tell StatsD that this metric timing is being sent
     *     sampled every 1/10th of the time.
     */
    public void time(String name, long value, double sampleRate) {
        time(name, value, sampleRate, null);
    }

    /**
     * Records the timing information for the specified metric.
     *
     * @param name
     *     the metric name
     * @param value
     *     the measured time
     * @param tags
     *     Only for DogStatsD compatible collectors.
     *     Assigned comma delimited tags. A tag value is delimited by colon.
     */
    public void time(String name, long value, String tags) {
        time(name, value, 0d, tags);
    }

    /**
     * Records the timing information for the specified metric.
     *
     * @param name
     *     the metric name
     * @param value
     *     the measured time
     * @param sampleRate
     *     the sampling rate being employed. For example, a rate of 0.1 would
     *     tell StatsD that this metric timing is being sent
     *     sampled every 1/10th of the time.
     * @param tags
     *     Only for DogStatsD compatible collectors.
     *     Assigned comma delimited tags. A tag value is delimited by colon.
     */
    public void time(String name, long value, double sampleRate, String tags) {
        submit(name, value, sampleRate, "ms", tags);
    }

    /**
     * Adds a value to the named histogram.
     *
     * @param name
     *     the histogram name
     * @param value
     *     the measured value
     */
    public void histo(String name, long value) {
        histo(name, value, null);
    }

    /**
     * Adds a value to the named histogram.
     *
     * @param name
     *     the histogram name
     * @param value
     *     the measured value
     * @param sampleRate
     *     the sampling rate being employed. For example, a rate of 0.1 would
     *     tell StatsD that this metric value is being sent
     *     sampled every 1/10th of the time.
     */
    public void histo(String name, long value, double sampleRate) {
        histo(name, value, sampleRate, null);
    }

    /**
     * Adds a value to the named histogram.
     *
     * @param name
     *     the histogram name
     * @param value
     *     the measured value
     * @param tags
     *     Only for DogStatsD compatible collectors.
     *     Assigned comma delimited tags. A tag value is delimited by colon.
     */
    public void histo(String name, long value, String tags) {
        histo(name, value, 0d, tags);
    }

    /**
     * Adds a value to the named histogram.
     *
     * @param name
     *     the histogram name
     * @param value
     *     the measured value
     * @param sampleRate
     *     the sampling rate being employed. For example, a rate of 0.1 would
     *     tell StatsD that this metric value is being sent
     *     sampled every 1/10th of the time.
     * @param tags
     *     Only for DogStatsD compatible collectors.
     *     Assigned comma delimited tags. A tag value is delimited by colon.
     */
    public void histo(String name, long value, double sampleRate, String tags) {
        submit(name, value, sampleRate, "h", tags);
    }

    /**
     * StatsD supports counting unique occurrences of events between flushes.
     * Call this method to records an occurrence of the specified named event.
     *
     * @param name
     *     the name of the set
     * @param id
     *     the value to be added to the set
     * @param tags
     *     Only for DogStatsD compatible collectors.
     *     Assigned comma delimited tags. A tag value is delimited by colon.
     */
    public void unique(String name, String id, String tags) {
        submit(name, id, "s", tags);
    }

    /**
     * StatsD supports counting unique occurrences of events between flushes.
     * Call this method to records an occurrence of the specified named event.
     *
     * @param name
     *     the name of the set
     * @param id
     *     the value to be added to the set
     */
    public void unique(String name, String id) {
        unique(name, id, null);
    }

    /**
     * Sends an event to a DogStatsD compatible collector
     *
     * @param title event name
     * @param text event text
     */
    public void event(String title, String text) {
        event(title, text, 0, null, null, null, null, null, null);
    }

    /**
     * Sends an event to a DogStatsD compatible collector
     *
     * @param title The event name
     * @param text The event text
     * @param tags
     *     Assigned comma delimited tags. A tag value is delimited by colon.
     */
    public void event(String title, String text, String tags) {
        event(title, text, 0, null, null, null, null, null, tags);
    }

    /**
     * Sends an event to a DogStatsD compatible collector
     *
     * @param title event name
     * @param text event text
     * @param timestamp
     *     Assign a timestamp to the event.
     *     0 means the current date
     * @param host
     *     Assign a hostname to the event.
     *     May be null
     * @param group
     *     Assign an aggregation key to the event, to group it with some others.
     *     May be null
     * @param sourceType
     *     Assign a source type to the event.
     *     May be null
     * @param priority
     *     {@linkplain Priority} - may be null for NORMAL
     * @param alertType
     *     {@linkplain AlertType} - may be null for INFO
     * @param tags
     *     Assigned comma delimited tags. A tag value is delimited by colon.
     */
    public void event(String title, String text, long timestamp, String host,
                      String group, String sourceType, Priority priority,
                      AlertType alertType, String tags) {
        StringBuilder sb = new StringBuilder("_e{");
        sb.append(title.length()).append(',')
          .append(text.length()).append('}');

        sb.append(':').append(title).append('|').append(text);

        if (timestamp >= 0) {
            sb.append("|d:").append(timestamp == 0 ? System.currentTimeMillis() : timestamp);
        }
        if (host != null) {
            sb.append("|h:").append(host);
        }
        if (group != null) {
            sb.append("|k:").append(group);
        }
        if (sourceType != null) {
            sb.append("|s:").append(sourceType);
        }
        if (priority != null) {
            sb.append("|p:").append(priority);
        }
        if (alertType != null) {
            sb.append("|t:").append(alertType);
        }
        appendTags(tags, sb);

        q.offer(sb.toString());
    }

    private void delta(String name, long value, double sampleRate, String tags) {
        StringBuilder sb = new StringBuilder(name);
        Formatter fmt = new Formatter(sb);

        sb.append(':');
        if (value > 0) {
            sb.append('+');
        } else if (value < 0) {
            sb.append('-');
        }
        sb.append(value).append('|').append('g');
        appendSampleRate(sampleRate, sb, fmt);
        appendTags(tags, sb);
        q.offer(sb.toString());
    }

    private void submit(String name, long value, double sampleRate, String type, String tags) {
        StringBuilder sb = new StringBuilder(name);
        Formatter fmt = new Formatter(sb);

        sb.append(':').append(value).append('|').append(type);
        appendSampleRate(sampleRate, sb, fmt);
        appendTags(tags, sb);
        q.offer(sb.toString());
    }

    private void submit(String name, String value, String type, String tags) {
        StringBuilder sb = new StringBuilder(name);

        sb.append(':').append(value).append('|').append(type);
        appendTags(tags, sb);
        q.offer(sb.toString());
    }

    private void appendTags(String tags, StringBuilder sb) {
        if (tags != null && !tags.isEmpty()) {
            sb.append("|#").append(tags);
        }
    }

    private void appendSampleRate(double sampleRate, StringBuilder sb, Formatter fmt) {
        if (sampleRate > 0) {
            sb.append("|@");
            fmt.format("%.3f", sampleRate);
        }
    }

    public static void main(String[] args) throws Exception {
        final Random r = new Random(System.nanoTime());

        final int batchSize = 100000000;
        final Client s = new Client();

        System.out.println("Hammering statsd...");
        long start = System.nanoTime();
        long last = System.nanoTime();
        for(int i = 0; i < batchSize; i++) {
//            s.gauge("io.btrace.statsd.jvm.counter.cnt1", i, "tag1:10,tag4:18,tag_x:blbko");
            s.gauge("io.btrace.statsd.jvm.counter.gauge1", i, "tag1:10,tag4:18,tag_x:blbko");
            s.gauge("io.btrace.statsd.jvm.counter.gauge1", i % 17, "tag4:18,tag_x:blbko");
            s.count("io.btrace.statsd.jvm.counter.cntr3", 1);
            s.time("io.btrace.statsd.system.resp.latency", System.nanoTime() - last);
            s.time("io.btrace.statsd.test.loop.time", (System.nanoTime() - last));
            s.unique("io.btrace.statsd.test.unique", String.valueOf(r.nextInt(100)));
            last = System.nanoTime();
//            LockSupport.parkNanos(20000);
        }
        long dur = (System.nanoTime() - start) / 1000000;

        System.out.println("Delivery rate = " + (batchSize * 6 / dur) + " msgs/ms");
    }
}
