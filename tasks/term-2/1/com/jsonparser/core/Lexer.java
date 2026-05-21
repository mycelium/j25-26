package com.jsonparser.core;

import com.jsonparser.exception.JsonException;
import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String source;
    private int pos;
    private final List<Token> tokens;

    public Lexer(String source) {
        this.source = source;
        this.pos = 0;
        this.tokens = new ArrayList<>();
    }

    public List<Token> scan() {
        tokens.clear();
        pos = 0;

        while (pos < source.length()) {
            char ch = source.charAt(pos);

            if (Character.isWhitespace(ch)) {
                pos++;
                continue;
            }

            switch (ch) {
                case '{' -> {
                    tokens.add(new Token(Token.Type.OBJECT_START));
                    pos++;
                }
                case '}' -> {
                    tokens.add(new Token(Token.Type.OBJECT_END));
                    pos++;
                }
                case '[' -> {
                    tokens.add(new Token(Token.Type.ARRAY_START));
                    pos++;
                }
                case ']' -> {
                    tokens.add(new Token(Token.Type.ARRAY_END));
                    pos++;
                }
                case ':' -> {
                    tokens.add(new Token(Token.Type.COLON));
                    pos++;
                }
                case ',' -> {
                    tokens.add(new Token(Token.Type.COMMA));
                    pos++;
                }
                case '"' -> readString();
                default -> {
                    if (ch == '-' || Character.isDigit(ch)) {
                        readNumber();
                    } else if (Character.isLetter(ch)) {
                        readKeyword();
                    } else {
                        throw new JsonException("Unexpected char: " + ch + " at " + pos);
                    }
                }
            }
        }
        return tokens;
    }

    private void readString() {
        pos++;
        StringBuilder sb = new StringBuilder();

        while (pos < source.length()) {
            char ch = source.charAt(pos);

            if (ch == '"') {
                pos++;
                tokens.add(new Token(Token.Type.TEXT, sb.toString()));
                return;
            }

            if (ch == '\\' && pos + 1 < source.length()) {
                char next = source.charAt(pos + 1);
                switch (next) {
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/' -> sb.append('/');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'u' -> {
                        if (pos + 5 >= source.length()) {
                            throw new JsonException("Incomplete unicode escape");
                        }
                        String hex = source.substring(pos + 2, pos + 6);
                        try {
                            sb.append((char) Integer.parseInt(hex, 16));
                            pos += 4;
                        } catch (NumberFormatException e) {
                            throw new JsonException("Invalid unicode: \\u" + hex);
                        }
                    }
                    default -> sb.append('\\').append(next);
                }
                pos += 2;
            } else {
                sb.append(ch);
                pos++;
            }
        }
        throw new JsonException("Unclosed string");
    }

    private void readNumber() {
        int start = pos;

        if (source.charAt(pos) == '-') pos++;
        while (pos < source.length() && Character.isDigit(source.charAt(pos))) pos++;

        if (pos < source.length() && source.charAt(pos) == '.') {
            pos++;
            while (pos < source.length() && Character.isDigit(source.charAt(pos))) pos++;
        }

        if (pos < source.length() && (source.charAt(pos) == 'e' || source.charAt(pos) == 'E')) {
            pos++;
            if (pos < source.length() && (source.charAt(pos) == '+' || source.charAt(pos) == '-')) pos++;
            while (pos < source.length() && Character.isDigit(source.charAt(pos))) pos++;
        }

        String num = source.substring(start, pos);
        tokens.add(new Token(Token.Type.NUM, num));
    }

    private void readKeyword() {
        int start = pos;
        while (pos < source.length() && Character.isLetter(source.charAt(pos))) pos++;
        String word = source.substring(start, pos);

        switch (word) {
            case "true" -> tokens.add(new Token(Token.Type.BOOL_TRUE, true));
            case "false" -> tokens.add(new Token(Token.Type.BOOL_FALSE, false));
            case "null" -> tokens.add(new Token(Token.Type.EMPTY, null));
            default -> throw new JsonException("Unknown keyword: " + word);
        }
    }
}