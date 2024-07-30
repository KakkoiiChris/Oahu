package kakkoiichris.oahu.util;

@SuppressWarnings("preview")
public class Aesthetics {
    public static final String ICON = "❀";
    public static final String UNDERLINE = "═";

    private static final char VERTICAL = '│';
    private static final char HORIZONTAL = '─';
    private static final char UP_LEFT = '╭';
    private static final char UP_RIGHT = '╮';
    private static final char DOWN_LEFT = '╰';
    private static final char DOWN_RIGHT = '╯';
    private static final char T_LEFT = '├';
    private static final char T_RIGHT = '┤';

    public static String wrapBox(String message) {
        var lines = message
            .lines()
            .toList();

        var maxWidth = lines
            .stream()
            .mapToInt(String::length)
            .max()
            .orElse(0);

        var horizontal = String.valueOf(HORIZONTAL).repeat(maxWidth + 2);

        var builder = new StringBuilder();

        builder.append(STR."\{UP_LEFT}\{horizontal}\{UP_RIGHT}\n");

        for (var line : lines) {
            if (line.isEmpty()) {
                builder.append(STR."\{T_LEFT}\{horizontal}\{T_RIGHT}\n");
            }
            else {
                builder.append(STR."\{VERTICAL} \{padEnd(line, maxWidth)} \{VERTICAL}\n");
            }
        }

        builder.append(STR."\{DOWN_LEFT}\{horizontal}\{DOWN_RIGHT}\n");

        return builder.toString();
    }

    public static String padStart(String s, int length) {
        if (length < 0) {
            throw new IllegalArgumentException(STR."Desired length \{length} is less than zero.");
        }

        if (length <= s.length()) {
            return s;
        }

        return String.valueOf(' ').repeat(length - s.length()) + s;
    }

    public static String padEnd(String s, int length) {
        if (length < 0) {
            throw new IllegalArgumentException(STR."Desired length \{length} is less than zero.");
        }

        if (length <= s.length()) {
            return s;
        }

        return s + String.valueOf(' ').repeat(length - s.length());
    }
}
