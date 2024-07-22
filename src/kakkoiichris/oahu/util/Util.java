/*#################################################*
 #    ____  _       _    _ _    _                  #
 #   / __ \ \|/\   | |  | | |  | |      /\         #
 #  | |  | | /  \  | |__| | |  | | ____/  \_       #
 #  | |  | |/ /\ \ |  __  | |  | |\         |      #
 #  | |__| / ____ \| |  | | |__| | \         \/|   #
 #   \____/_/    \_\_|  |_|\____/   \___/\__   \   #
 #                                          \___\  #
 #        Copyright (C) 2019, KakkoiiChris         #
 *#################################################*/
package kakkoiichris.oahu.util;

import java.util.Optional;

public class Util {
    public static Optional<Integer> parseInt(String s, int radix) {
        try {
            return Optional.of(Integer.parseInt(s, radix));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static Optional<Double> parseNumber(String s) {
        try {
            return Optional.of(Double.parseDouble(s));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static <X> Optional<X> cast(Class<X> clazz, Object object) {
        X x;

        try {
            x = clazz.cast(object);
        }
        catch (ClassCastException e) {
            return Optional.empty();
        }

        return Optional.ofNullable(x);
    }

    public static <X extends Enum<X>> Optional<X> getEntry(Class<X> enm, String name) {
        try {
            return Optional.of(Enum.valueOf(enm, name));
        }
        catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}