package io.btrace.jstatsd.server.api.metrics;

import io.btrace.jstatsd.server.api.LedgerEntry;
import io.btrace.jstatsd.server.api.Token;

import java.util.*;

public abstract class Metric implements LedgerEntry {
    public static final class Identifier {
        private final Token key;
        private final Map<Token, Token> tags;

        private Token unique;

        private boolean hashCalculated = false;
        private int hash = 0;

        Identifier(Token key) {
            this(key, null);
        }

        Identifier(Token key, Map<Token, Token> tags) {
            this.key = key;
            this.tags = tags;
        }

        /**
        * A component key comprised of the metric name and its tags.
        * <p>
        * !!! Important !!! Computing this key is not trivial so cache it
        * when used more than once!
        * </p>
        *
        * @return component (metric name, tags) key
        */
       Token getUnique() {
           if (tags == null || tags.isEmpty()) {
               return key;
           }

           if (unique != null) {
               return unique;
           }

           byte[] newarr = new byte[key.length() * 2];
           List<Token> tagList = new ArrayList<>(tags.keySet());
           Collections.sort(tagList, Token.STRING_COMPARATOR);
           int pos = 0;
           newarr[pos++] = '^';
           pos = key.copyTo(newarr, pos);
           newarr[pos++] = '#';
           boolean first = true;
           for(Token tk : tagList) {
               if (first) {
                   first = false;
               } else {
                   newarr[pos++] = ',';
                   if (pos > newarr.length) {
                       newarr = Arrays.copyOf(newarr, newarr.length * 2);
                   }
               }
               int copied = - 1;
               while ((copied = tk.copyTo(newarr, pos)) == -1) {
                   newarr = Arrays.copyOf(newarr, newarr.length * 2);
               }
               pos += copied;
               Token val = tags.get(tk);
               if (val.length() > 0) {
                   if (pos > newarr.length) {
                       newarr = Arrays.copyOf(newarr, newarr.length * 2);
                   }
                   newarr[pos++] = ':';
                   while ((copied = val.copyTo(newarr, pos)) == -1) {
                       newarr = Arrays.copyOf(newarr, newarr.length * 2);
                   }
                   pos += copied;
               }
           }
           return unique = new Token(Arrays.copyOf(newarr, pos), 0, pos);
        }

        public Token getKey() {
            return key;
        }

        public Map<Token, Token> getTags() {
            return tags;
        }

        @Override
        public int hashCode() {
            if (hashCalculated) {
                return this.hash;
            }
            int hash = 7;
            hash = 89 * hash + Objects.hashCode(this.key);
            if (this.tags != null) {
                for(Map.Entry<Token, Token> e : this.tags.entrySet()) {
                    hash = 89 * hash + Objects.hashCode(e.getKey());
                    hash = 89 * hash + Objects.hashCode(e.getValue());
                }
            }
            this.hash = hash;
            this.hashCalculated = true;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Identifier other = (Identifier) obj;
            return Objects.equals(this.getUnique(), other.getUnique());
        }

        @Override
        public String toString() {
            return "Identifier{" + "key=" + key + ", tags=" + tags + '}';
        }
    }

    private String name = null;

    private final Identifier key;

    protected Metric(Identifier key) {
        this.key = key;
    }

    public Identifier getId() {
        return key;
    }

    public String getName() {
        if (name == null) {
            name = key.key.asString();
        }
        return name;
    }
}
