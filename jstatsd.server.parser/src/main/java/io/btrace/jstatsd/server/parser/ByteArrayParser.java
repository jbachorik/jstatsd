package io.btrace.jstatsd.server.parser;

import io.btrace.jstatsd.server.api.Token;

import java.util.Arrays;

public final class ByteArrayParser {
    final static char EOM = '\n';

    static final byte[] EMPTY_STRING = new byte[0];

    private final boolean[] controlCharArr = new boolean[256];
    private final byte[] src;
    private int srcPos, markPos;

    ByteArrayParser(byte[] src, char[] controlChars) {
        this.src = src;
        this.srcPos = 0;
        Arrays.fill(controlCharArr, false);
        for(char c : controlChars) {
            controlCharArr[c] = true;
        }
    }

    public static char[] delimiters(String delimiters) {
        char[] delims = delimiters.toCharArray();
        Arrays.sort(delims);
        return delims;
    }

    public Token nextToken(char delimiter) {
        return nextToken(delimiter, false);
    }

    public Token nextToken(char delimiter, boolean mayStop) {
        // first skip any whitespace tokens
        while (srcPos < src.length) {
            char ch = (char)src[srcPos];
            if (ch != ' ' && ch != '\t') {
                break;
            }
            srcPos++;
        }
        // now try to locate the token designated by the provided delimiter
        int start = srcPos;
        while (srcPos < src.length) {
            int pos = srcPos++;
            char ch = (char)src[pos];
            if (ch == delimiter) {
                Token val = pos > start ? new Token(src, start, pos) : Token.EMPTY;
                return val;
            }
            if (ch == EOM) {
                if (mayStop) {
                    Token val = pos > start ? new Token(src, start, pos) : Token.EMPTY;
                    return val;
                }
            } else {
                if (controlCharArr[ch]) {
                    // unexpected controll character
                    return null;
                }
            }
        }
        if (mayStop) {
            Token val = srcPos > start ? new Token(src, start, srcPos) : Token.EMPTY;
            return val;
        }
        return null;
    }

    public void back() {
        srcPos--;
    }

    public void forward() {
        srcPos++;
    }

    public void mark() {
        markPos = srcPos;
    }

    public void reset() {
        srcPos = markPos;
    }

    public void rewind() {
        markPos = 0;
        srcPos = 0;
    }
}
