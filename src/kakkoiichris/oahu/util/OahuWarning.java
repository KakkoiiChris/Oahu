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
import kakkoiichris.oahu.parser.Expr;

import static kakkoiichris.oahu.util.Aesthetics.ICON;

@SuppressWarnings("preview")
public class OahuWarning {
    private static void warn(Stage stage, String message, Source source, Context context) {
        var row = context.row();
        var line = source.getLine(context.row());

        var spacing = " ".repeat(context.column() + (String.valueOf(context.row()).length() + 1));
        var underline = Aesthetics.UNDERLINE.repeat(context.length());

        var string = STR."""
            O'ahu \{stage} Warning \{ICON} \{message}!

            \{row}| \{line}
            \{spacing}\{underline}""";

        System.err.println(Aesthetics.wrapBox(string));
    }

    private static void warn(Stage stage, Source source, String message) {
        var string = STR."O'ahu Linker Error \{ICON} \{message}!";

        System.err.println(Aesthetics.wrapBox(string));
    }

    public static void forLexer(String message, Source source, Context context) {
        warn(Stage.LEXER, message, source, context);
    }

    public static void forParser(String message, Source source, Context context) {
        warn(Stage.PARSER, message, source, context);
    }

    public static void discardedName(Source source, Context context) {
        forParser("Variable name '_' is always discarded; consider renaming", source, context);
    }

    public static void vagueName(Source source, Context context) {
        forParser("Variable names consisting of only underscores are vague; consider renaming", source, context);
    }

    public static void duplicateLink(Expr.Name name, Source source) {
        warn(Stage.LINKER, String.format("File '%s' already linked!", name), source, name.context());
    }
}
