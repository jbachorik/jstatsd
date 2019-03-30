package io.btrace.jstatsd.server.sinks;

import io.btrace.jstatsd.server.api.Snapshot;
import io.btrace.jstatsd.server.api.metrics.Counter;
import io.btrace.jstatsd.server.api.metrics.Gauge;
import io.btrace.jstatsd.server.spi.Sink;
import org.apache.log4j.Logger;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

public class ElasticSearchSink extends Sink {
    private final static Logger LOG = Logger.getLogger(ElasticSearchSink.class);

    private final static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd");

    private final AtomicLong counter = new AtomicLong();
    private final Client client;

    public ElasticSearchSink() {
        Client c = null;
        try {
            c = TransportClient.builder().build().addTransportAddress(
                new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300)
            );
        } catch (IOException e) {
            LOG.error("Error establishing ElasticSearch connection", e);
        }
        client = c;
    }

    @Override
    public String id() {
        return "elastic-search";
    }

    @Override
    public void accept(Snapshot s) {
        if (s.isEmpty()) {
            return;
        }
        Date d = new Date();
        String index = "statsd-" + DATE_FORMAT.format(d);
        BulkRequestBuilder brb = client.prepareBulk();
        indexCounters(s, brb, index, d);
        indexGauges(s, brb, index, d);
        BulkResponse br = brb.get();
        if (br.hasFailures()) {
            for(BulkItemResponse bir : br) {
                System.err.println("!!! " + bir.getFailureMessage());
            }
        }
    }

    private void indexCounters(Snapshot s, BulkRequestBuilder brb, String index, Date ts) {
        for(Counter c : s.getCounters()) {
            IndexRequestBuilder irb = prepareDoc(index, "counter");
            String key = c.getId().getKey().asString();
            String[] nsParts = extractParts(key);
            irb.setSource(
                "ns", nsParts[0],
                "grp", nsParts[1],
                "tgt", nsParts[2],
                "act", nsParts[3],
                "val", c.count,
                "@ts", ts
            );
            brb.add(irb);
        }
    }

    private void indexGauges(Snapshot s, BulkRequestBuilder brb, String index, Date ts) {
        for(Gauge g : s.getGauges()) {
            IndexRequestBuilder irb = prepareDoc(index, "gauge");
            String key = g.getId().getKey().asString();
            String[] nsParts = extractParts(key);
            irb.setSource(
                "ns", nsParts[0],
                "grp", nsParts[1],
                "tgt", nsParts[2],
                "act", nsParts[3],
                "val", g.value,
                "@ts", ts
            );
            brb.add(irb);
        }
    }

    private IndexRequestBuilder prepareDoc(String index, String type) {
        counter.incrementAndGet();
        return client.prepareIndex(index, type);
    }

    private String[] extractParts(String key) {
        String[] nsParts = new String[4];
        Arrays.fill(nsParts, "");
        int idx = 0, lastIdx = 0, cntr = 0;
        while (cntr < 3 && (idx = key.indexOf('.', lastIdx)) > -1) {
            nsParts[cntr++] = key.substring(lastIdx, idx);
            lastIdx = idx + 1;
        }
        if (cntr == 3) {
            nsParts[cntr] = key.substring(lastIdx);
        }
        return nsParts;
    }

    @Override
    public void report(PrintStream s) {
        s.append("=> Backend[").append(id()).append("] (documents: ").append(String.valueOf(counter.get())).append(")\n");
    }
}
