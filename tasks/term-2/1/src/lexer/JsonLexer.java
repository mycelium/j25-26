package lexer;

import java.util.ArrayList;
import java.util.List;

public class JsonLexer {
    private final String input;
    public final int length;
    public JsonLexer(String input) {
        this.input = input;
        this.length = input.length();
    }
    public List<Lexeme> analyzeJson() {
        List<Lexeme> lexemeList = new ArrayList<>();
        int i = 0;
        while (i < length) {
            char c = input.charAt(i);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                i++;
                continue;
            }
            switch (c) {
                case '{':
                    lexemeList.add(new Lexeme(Lexeme.LexemeType.LeftBRACE, "{"));
                    i++;
                    break;
                case '}':
                lexemeList.add(new Lexeme(Lexeme.LexemeType.RightBRACE, "}"));
                i++;
                break;

                case '[':
                    lexemeList.add(new Lexeme(Lexeme.LexemeType.LeftBRACKET, "["));
                    i++;
                    break;

                case ']':
                    lexemeList.add(new Lexeme(Lexeme.LexemeType.RightBRACKET, "]"));
                    i++;
                    break;

                case ',':
                    lexemeList.add(new Lexeme(Lexeme.LexemeType.COMMA, ","));
                    i++;
                    break;

                case ':':
                    lexemeList.add(new Lexeme(Lexeme.LexemeType.COLON, ":"));
                    i++;
                    break;
                case '"':
                    i++;
                    StringBuilder stringBuilder = new StringBuilder();
                    while (i < length && input.charAt(i) != '"') {
                        stringBuilder.append(input.charAt(i));
                        i++;
                    }
                    if (i < length && input.charAt(i) == '"') {
                        i++;
                    } else {
                        throw new RuntimeException("Unclosed string literal");
                    }
                    lexemeList.add(new Lexeme(Lexeme.LexemeType.STRING, stringBuilder.toString()));
                    break;
                case '-':
                case '0': case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':
                    StringBuilder numberBuilder = new StringBuilder();
                    while (i < length) {
                        char current = input.charAt(i);
                        if (Character.isDigit(current) || current == '.' ||
                                current == 'e' || current == 'E' ||
                                current == '+' || current == '-') {
                            numberBuilder.append(current);
                            i++;
                        } else {
                            break;
                        }
                    }
                    String numStr = numberBuilder.toString();
                    lexemeList.add(new Lexeme(Lexeme.LexemeType.NUMBER, numStr));
                    break;
                case 't':
                    if (i + 3 < length &&
                            input.charAt(i + 1) == 'r' &&
                            input.charAt(i + 2) == 'u' &&
                            input.charAt(i + 3) == 'e') {
                        lexemeList.add(new Lexeme(Lexeme.LexemeType.BOOL, "true"));
                        i += 4;
                    } else {
                        throw new RuntimeException("Invalid literal");
                    }
                    break;
                case 'f':
                    if (i + 4 < length && input.charAt(i + 1) == 'a' &&
                            input.charAt(i + 2) == 'l' && input.charAt(i + 3) == 's' &&
                            input.charAt(i + 4) == 'e') {
                        lexemeList.add(new Lexeme(Lexeme.LexemeType.BOOL, "false"));
                        i += 5;
                    } else {
                        throw new RuntimeException("Invalid literal");
                    }
                    break;
                case 'n':
                    if (i + 3 < length && input.charAt(i + 1) == 'u' &&
                            input.charAt(i + 2) == 'l' && input.charAt(i + 3) == 'l') {
                        lexemeList.add(new Lexeme(Lexeme.LexemeType.NULL, "null"));
                        i += 4;
                    } else {
                        throw new RuntimeException("Invalid literal");
                    }
                    break;
                default:
                    throw new RuntimeException("Unexpected character: '" + c + "' at position " + i);
            }
        }
        return lexemeList;
    }
}
