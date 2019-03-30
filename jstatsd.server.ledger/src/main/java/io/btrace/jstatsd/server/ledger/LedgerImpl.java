package io.btrace.jstatsd.server.ledger;

import gnu.trove.TLongArrayList;
import gnu.trove.TLongProcedure;
import io.btrace.jstatsd.server.api.Ledger;
import io.btrace.jstatsd.server.api.Snapshot;
import io.btrace.jstatsd.server.api.Token;
import io.btrace.jstatsd.server.api.impl.SnapshotImpl;
import io.btrace.jstatsd.server.api.metrics.*;
import io.btrace.jstatsd.server.api.metrics.Metric.Identifier;
import net.openhft.koloboke.collect.map.ObjLongMap;
import net.openhft.koloboke.collect.map.hash.HashObjLongMaps;

import javax.inject.Singleton;
import java.util.*;

/**
 * Running accumulator of incoming metrics.
 * Allows for capturing snapshots of aggregated data.
 */
@Singleton
public final class LedgerImpl extends Ledger {
    private static final class MSnapshot implements SnapshotImpl {
        private final class LongSum implements TLongProcedure {
            private long sum = 0L;
            @Override
            public boolean execute(long l) {
                sum += l;
                return true;
            }
        }

        private final List<Counter> counters;
        private final List<Gauge> gauges;
        private final List<Histo> series;
        private final List<Event> events;
        private final List<Counter> uniques;

        public MSnapshot(LedgerImpl r) {
            counters = new ArrayList<>(r.counterVals.size());
            for(Metric.Identifier i : r.counterVals.keySet()) {
                long val = r.counterVals.getLong(i);
                counters.add(new Counter(i, val));
            }

            gauges = new ArrayList<>(r.gauges.size());
            for(Metric.Identifier i : r.gauges.keySet()) {
                gauges.add(new Gauge(i, r.gauges.getLong(i), 0));
            }

            series = new ArrayList<>(r.series.size());
            for(Map.Entry<Identifier, TLongArrayList> e : r.series.entrySet()) {
                TLongArrayList l = e.getValue();
                l.sort();
                long min = l.get(0);
                long max = l.get(l.size() - 1);
                long median = findPercentile(l, 50);
                long per95 = findPercentile(l, 95);
                long per99 = findPercentile(l, 99);

                LongSum s = new LongSum();
                l.forEach(s);
                long avg = s.sum / l.size();

                series.add(new Histo(e.getKey(), avg, max, min, median, per95, per99));
            }

            uniques = new ArrayList<>(r.uniques.size());
            for(Map.Entry<Identifier, Set<Token>> e : r.uniques.entrySet()) {
                uniques.add(new Counter(e.getKey(), e.getValue().size()));
            }

            events = new ArrayList<>(r.events);
        }

        @Override
        public boolean isEmpty() {
            return counters.isEmpty() && gauges.isEmpty() && series.isEmpty();
        }

        @Override
        public List<Counter> getCounters() {
            return Collections.unmodifiableList(counters);
        }

        @Override
        public List<Gauge> getGauges() {
            return Collections.unmodifiableList(gauges);
        }

        @Override
        public List<Histo> getSeries() {
            return Collections.unmodifiableList(series);
        }

        @Override
        public List<Event> getEvents() {
            return Collections.unmodifiableList(events);
        }

        @Override
        public List<Counter> getUniques() {
            return Collections.unmodifiableList(uniques);
        }

        private long findPercentile(TLongArrayList l, int percentile) {
            int size = l.size();
            int idx = (size * percentile / 100);
            if (idx < size - 1) {
                return (l.get(idx) + l.get(idx + 1)) / 2;
            }
            return l.get(idx);
        }

        @Override
        public String toString() {
            return "Snapshot{" + "counters=" + counters + ", gauges=" + gauges + ", series=" + series + '}';
        }
    }

    private final ObjLongMap<Metric.Identifier> counterVals = HashObjLongMaps.newMutableMap();
    private final ObjLongMap<Metric.Identifier> counterCounts = HashObjLongMaps.newMutableMap();
    private final ObjLongMap<Metric.Identifier> gauges = HashObjLongMaps.newMutableMap();
    private final Map<Metric.Identifier, Set<Token>> uniques = new HashMap<>();
    private final Map<Metric.Identifier, TLongArrayList> series = new HashMap<>();
    private final List<Event> events = new LinkedList<>();

    /**
     * Increment the counter value
     * @param c {@linkplain Counter} metric instance
     */
    @Override
    public void add(Counter c) {
        counterVals.addValue(c.getId(), c.count);
        counterCounts.addValue(c.getId(), 1);
    }

    /**
     * Adjust (increase/decrease) the gauge value
     * @param g {@linkplain Gauge} metric instance
     */
    @Override
    public void add(Gauge g) {
        gauges.addValue(g.getId(), g.value);
    }

    /**
     * Set the gauge value
     * @param g {@linkplain Gauge} metric instance
     */
    @Override
    public void set(Gauge g) {
        gauges.put(g.getId(), g.value);
    }

    /**
     * Record a new value for statistically analyzed metric
     * @param a {@linkplain Histo} metric instance
     */
    @Override
    public void add(Histo a) {
        Metric.Identifier k = a.getId();
        TLongArrayList tla = series.get(k);
        if (tla == null) {
            tla = new TLongArrayList();
            series.put(k, tla);
        }
        tla.add(a.avg);
    }

    @Override
    public void add(Event e) {
        events.add(e);
    }

    @Override
    public void add(Unique u) {
        Metric.Identifier k = u.getId();
        Set<Token> vals = uniques.get(k);
        if (vals == null) {
            vals = new HashSet<>();
            uniques.put(k, vals);
        }
        vals.add(u.getValue());
    }

    @Override
    public Snapshot snapshot() {
        Snapshot s = new Snapshot(new MSnapshot(this));
        counterVals.clear();
        counterCounts.clear();
        series.clear();
        return s;
    }
}
