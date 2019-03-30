package io.btrace.jstatsd.server.parser;

import io.btrace.jstatsd.server.api.LedgerEntry;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class LedgerEntryParserTest {
    private final static byte[] payloadSingle = "accounts.authentication.login.time:320|ms|@0.1|#hasTags,tag1=20,tag_puk=3".getBytes(Charset.forName("utf8"));

    private static final Map<String, Boolean> validityInfo = new HashMap<>();

    static {
        validityInfo.put("1,2,1,2,0", Boolean.TRUE);
        validityInfo.put("1,2,1,0,0", Boolean.TRUE);
        validityInfo.put("1,2,2,2,0", Boolean.TRUE);
        validityInfo.put("1,2,2,0,0", Boolean.TRUE);
        validityInfo.put("1,2,3,2,0", Boolean.TRUE);
        validityInfo.put("1,2,3,0,0", Boolean.TRUE);
        validityInfo.put("1,2,4,2,0", Boolean.TRUE);
        validityInfo.put("1,2,4,0,0", Boolean.TRUE);
        validityInfo.put("1,2,5,2,0", Boolean.TRUE);
        validityInfo.put("1,2,5,0,0", Boolean.TRUE);

        validityInfo.put("1,2,1,2,1", Boolean.TRUE);
        validityInfo.put("1,2,1,0,1", Boolean.TRUE);
        validityInfo.put("1,2,2,2,1", Boolean.TRUE);
        validityInfo.put("1,2,2,0,1", Boolean.TRUE);
        validityInfo.put("1,2,3,2,1", Boolean.TRUE);
        validityInfo.put("1,2,3,0,1", Boolean.TRUE);
        validityInfo.put("1,2,4,2,1", Boolean.TRUE);
        validityInfo.put("1,2,4,0,1", Boolean.TRUE);
        validityInfo.put("1,2,5,2,1", Boolean.TRUE);
        validityInfo.put("1,2,5,0,1", Boolean.TRUE);

        validityInfo.put("1,2,1,2,2", Boolean.TRUE);
        validityInfo.put("1,2,1,0,2", Boolean.TRUE);
        validityInfo.put("1,2,2,2,2", Boolean.TRUE);
        validityInfo.put("1,2,2,0,2", Boolean.TRUE);
        validityInfo.put("1,2,3,2,2", Boolean.TRUE);
        validityInfo.put("1,2,3,0,2", Boolean.TRUE);
        validityInfo.put("1,2,4,2,2", Boolean.TRUE);
        validityInfo.put("1,2,4,0,2", Boolean.TRUE);
        validityInfo.put("1,2,5,2,2", Boolean.TRUE);
        validityInfo.put("1,2,5,0,2", Boolean.TRUE);

        validityInfo.put("1,2,1,2,3", Boolean.TRUE);
        validityInfo.put("1,2,1,0,3", Boolean.TRUE);
        validityInfo.put("1,2,2,2,3", Boolean.TRUE);
        validityInfo.put("1,2,2,0,3", Boolean.TRUE);
        validityInfo.put("1,2,3,2,3", Boolean.TRUE);
        validityInfo.put("1,2,3,0,3", Boolean.TRUE);
        validityInfo.put("1,2,4,2,3", Boolean.TRUE);
        validityInfo.put("1,2,4,0,3", Boolean.TRUE);
        validityInfo.put("1,2,5,2,3", Boolean.TRUE);
        validityInfo.put("1,2,5,0,3", Boolean.TRUE);
        for(int a=0;a<2;a++) {
            for(int b=0;b<3;b++) {
                for(int c=0;c<6;c++) {
                    for(int d=0;d<3;d++) {
                        for(int e=0;e<4;e++) {
                            String key = a + "," + b + "," + c + "," + d + "," + e;
                            if (!validityInfo.containsKey(key)) {
                                validityInfo.put(key, Boolean.FALSE);
                            }
                        }
                    }
                }
            }
        }
    }

    @Before
    public void setUp() {
    }

    @Test
    public void testMessage() {
        System.out.println("message");
        for(Map.Entry<String, Boolean> e : validityInfo.entrySet()) {
            String[] vals = e.getKey().split(",");
            int[] params = new int[vals.length];
            for(int i = 0; i < params.length; i++) {
                params[i] = Integer.parseInt(vals[i]);
            }
            String load = generatePayload(params[0], params[1], params[2], params[3], params[4]);
            System.out.println("=== checking: " + load + " - expecting [" + e.getValue() + "]");
            LedgerEntryParser mp = new LedgerEntryParser(load.getBytes(Charset.forName("ascii")));
            LedgerEntry m = mp.nextMessage();
            System.out.println("===> message = " + m);
            assertEquals(e.getValue(), m != null);
        }
    }

    private String generatePayload(int name, int value, int type, int sample, int tags) {
        StringBuilder sb = new StringBuilder();
        if (name == 1) {
            sb.append("ns.cat.part.name.sub");
        }
        if (value > 0) {
            sb.append(":");
        }
        if (value == 2) {
            sb.append("100");
        }
        if (type > 0) {
            sb.append("|");
        }
        switch (type) {
            case 1: {
                sb.append('c');
                break;
            }
            case 2: {
                sb.append('g');
                break;
            }
            case 3: {
                sb.append("ms");
                break;
            }
            case 4: {
                sb.append('h');
                break;
            }
            case 5: {
                sb.append('u');
                break;
            }
        }
        if (sample > 0) {
            sb.append("|");
        }
        if (sample == 2) {
            sb.append("@0.2");
        } else if (sample == 1) {
            sb.append("@");
        }
        if (tags > 0) {
            sb.append("|");
        }
        switch (tags) {
            case 1: {
                sb.append("#tag");
                break;
            }
            case 2: {
                sb.append("#tag,tag:val");
                break;
            }
            case 3: {
                sb.append("#tag:val,tag");
            }
        }
        return sb.toString();
    }

}
