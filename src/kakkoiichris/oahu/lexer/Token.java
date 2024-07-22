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

public record Token<T extends TokenType>(Context context, T type, Object value) {
    public Token(Context loc, T type) {
        this(loc, type, null);
    }
    
    @Override
    public String toString() {
        if (value == null) {
            return String.format("%s%s", type, context);
        }
        
        return String.format("%s%s (%s)", type, context, value);
    }
}
