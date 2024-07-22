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

import kakkoiichris.oahu.lexer.Context;

public class OahuWarning {
    private static void warn(String stage, String message, Context context) {
        System.err.printf("Warning @ Nano %s -> %s!%s%n", stage, message, context);
    }
    
    private static void forParser(String message, Context context) {
        warn("Parser", message, context);
    }
    
    public static void discardedName(Context context) {
        forParser("Variable name '_' is always discarded; consider renaming", context);
    }
    
    public static void vagueName(Context context) {
        forParser("Variable names consisting of only underscores are vague; consider renaming", context);
    }
    
    private static void forScript(String message, Context context) {
        warn("Script", message, context);
    }
}
