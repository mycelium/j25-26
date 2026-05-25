package jsonlab;

public class JsonException extends RuntimeException {
    private final int line;
    private final int column;
    private final int position;

    public JsonException(String message) {
        this(message, -1, -1, -1);
    }

    public JsonException(String message, int pos, int line, int col) {
        super(formatError(message, pos, line, col));
        this.position = pos;
        this.line = line;
        this.column = col;
    }

    public JsonException(String message, Throwable cause) {
        super(message, cause);
        this.position = -1;
        this.line = -1;
        this.column = -1;
    }

    private static String formatError(String msg, int pos, int line, int col) {
        if (line < 0) return msg;
        return String.format("%s [line %d, col %d, pos %d]", msg, line, col, pos);
    }

    public int getLine() { return line; }
    public int getColumn() { return column; }
    public int getPosition() { return position; }
}