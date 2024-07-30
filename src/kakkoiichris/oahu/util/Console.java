package kakkoiichris.oahu.util;

import java.util.Scanner;

public class Console {
    private static final Scanner INPUT = new Scanner(System.in);

    public static void print(Object value) {
        System.out.print(value);
    }

    public static void printf(String format, Object... args) {
        System.out.printf(format, args);
    }

    public static void println(Object value) {
        System.out.println(value);
    }

    public static void newln() {
        System.out.println();
    }

    public static String read() {
        return INPUT.next();
    }

    public static String readln() {
        return INPUT.nextLine();
    }

    public static String truncate(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((int) value);
        }

        return String.valueOf(value);
    }

    public static String quotify(Object value) {
        return String.valueOf(value);
    }
}
