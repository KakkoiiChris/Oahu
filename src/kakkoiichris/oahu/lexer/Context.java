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
package kakkoiichris.oahu.lexer;

public record Context(String name, int row, int column, int position, int length) {
    public static Context none() {
        return new Context("", 0, 0, 0, 0);
    }

    public Context rangeTo(Context other) {
        return new Context(name, row, column, position, other.position - position);
    }

    @SuppressWarnings("preview")
    @Override
    public String toString() {
        if (name.isEmpty()) {
            return "";
        }

        return STR." @ \{name} (\{row}, \{column})";
    }
}
