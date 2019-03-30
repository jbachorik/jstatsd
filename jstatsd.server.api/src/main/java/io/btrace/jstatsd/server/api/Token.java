package io.btrace.jstatsd.server.api;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Comparator;

public final class Token {
    public static final Comparator<Token> STRING_COMPARATOR = new Comparator<Token>() {
        @Override
        public int compare(Token o1, Token o2) {
            int l1 = o1.length();
            int l2 = o2.length();
            if (l1 > l2) {
                return 1;
            } else if (l1 < l2) {
                return -1;
            }

            for(int i = 0; i < l1; i++) {
                int d1 = o1.data[o1.from + i];
                int d2 = o2.data[o2.from + i];
                if (d1 > d2) {
                    return 1;
                } else if (d1 < d2) {
                    return -1;
                }
            }

            return 0;
        }
    };
    static final Charset CHARSET = Charset.forName("ascii");
    public static final Token EMPTY = new Token(new byte[0], 0, 0);

    private final byte[] data;
    private final int from, to;

    private volatile boolean hashComputed = false;
    private int hash;

    public Token(byte[] data, int from, int to) {
        this.data = data;
        this.from = from;
        this.to = to;
    }

    public final boolean isEmpty() {
        return from >= to;
    }

    public final int length() {
        return from <= to ? to - from : 0;
    }

    public final byte[] getValue() {
        return data != null ? Arrays.copyOfRange(data, from, to) : null;
    }

    public final String asString() {
        byte[] val = getValue();
        if (val == null) {
            return null;
        }
        if (val.length == 0) {
            return "";
        }
        return new String(val, CHARSET).trim();
    }

    public final byte charAt(int idx) {
        return data[idx];
    }

    public final int getDeltaPrefix() {
        byte b = data[from];
        if (b == '+') {
            return 1;
        } else if (b == '-') {
            return -1;
        }
        return 0;
    }

    public final boolean isInteger() {
        for(int i = from; i < to; i++) {
            final byte b = data[i];
            if (b < '0' || b > '9') {
                if (b == '+' || b == '-') {
                    // leading '+' or '-' is valid
                    if (i == from) continue;
                }
                return false;
            }
        }
        return true;
    }

    public final long asLong() throws NumberFormatException {
        if (!isInteger()) {
            throw new NumberFormatException(asString() + " is not a valid integer number");
        }
        long acc = 0L;
        int mult = 1;
        for(int i = from; i < to; i ++) {
            final byte b = data[i];
            if (b == '+' || b == '-') {
                if (b == '-') {
                    mult = -1;
                }
                continue;
            }
            acc = acc * 10 + (data[i] - '0');
        }
        return acc * mult;
    }

    public final int asInt() throws NumberFormatException {
        if (!isInteger()) {
            throw new NumberFormatException(asString() + " is not a valid integer number");
        }
        int acc = 0;
        int mult = 1;
        for(int i = from; i < to; i ++) {
            final byte b = data[i];
            if (b == '+' || b == '-') {
                if (b == '-') {
                    mult = -1;
                }
                continue;
            }
            acc = acc * 10 + (data[i] - '0');
        }
        return acc * mult;
    }

    public final Token[] split(char c) {
        Token[] arr = new Token[2];
        int idx = 0;
        int start = from;
        int end = from;
        for(end = from; end < to; end++) {
            if (data[end] == c && end > start) {
                arr[idx] = new Token(data, start, end);
                start = ++end;
                if (++idx == arr.length) {
                    arr = Arrays.copyOf(arr, arr.length * 2);
                }
            }
        }
        if (end > start) {
            arr[idx++] = new Token(data, start, end);
        }
        return Arrays.copyOf(arr, idx);
    }

    public final int copyTo(byte[] dst, int dstPos) {
        int len = length();
        if (dstPos + len < dst.length) {
            if (len > 0) {
                System.arraycopy(data, from, dst, dstPos, len);
            }
            return len;
        }
        return -1;
    }

    public final Token derive(int from, int to) {
        return new Token(data, this.from + from, this.from + to);
    }

    public final Token derive(int from) {
        return new Token(data, this.from + from, to);
    }

    @Override
    public final int hashCode() {
        if (!hashComputed) {
            int result = 7;
            if (from >= to || data == null) {
                result = 0;
            } else {
                for (int i = from; i < to; i++) {
                    result = 37 * result + data[i];
                }

                result = 31 * result + this.from;
                result = 31 * result + this.to;
            }

            hash = result;
            hashComputed = true;
        }
        return hash;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Token other = (Token) obj;
        if (this.from != other.from) {
            return false;
        }
        if (this.to != other.to) {
            return false;
        }
        for(int i = from; i < to; i++) {
            if (this.data[i] != other.data[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "Token{" + asString() + '}';
    }
}
