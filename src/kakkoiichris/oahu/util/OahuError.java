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
import kakkoiichris.oahu.lexer.TokenType;
import kakkoiichris.oahu.parser.Expr;
import kakkoiichris.oahu.runtime.Redirect;

import static kakkoiichris.oahu.util.Aesthetics.ICON;

@SuppressWarnings("preview")
public class OahuError extends RuntimeException {
    private OahuError(String message) {
        super(message);
    }

    private static OahuError error(Stage stage, String message, Source source, Context context) {
        var row = context.row();
        var line = source.getLine(context.row());

        var spacing = " ".repeat(context.column() + (String.valueOf(context.row()).length() + 1));
        var underline = Aesthetics.UNDERLINE.repeat(context.length());

        var string = STR."""
            O'ahu \{stage} Error \{ICON} \{message}!

            \{row}| \{line}
            \{spacing}\{underline}""";

        return new OahuError(Aesthetics.wrapBox(string));
    }

    private static OahuError error(String message) {
        var string = STR."O'ahu Linker Error \{ICON} \{message}!";

        return new OahuError(Aesthetics.wrapBox(string));
    }

    private static OahuError forLexer(String message, Source source, Context context) {
        return error(Stage.LEXER, message, source, context);
    }

    public static OahuError earlyEndOfFile(Source source, Context context) {
        return forLexer("Reached end of file early", source, context);
    }

    public static OahuError illegalCharacter(char illegal, Source source, Context context) {
        return forLexer("Character '%c' is illegal".formatted(illegal), source, context);
    }

    public static OahuError illegalCharacterEscape(char escape, Source source, Context context) {
        return forLexer("Character escape '\\%c' is illegal".formatted(escape), source, context);
    }

    public static OahuError invalidCharacter(char invalid, char expected, Source source, Context context) {
        return forLexer("Character '%c' is invalid; expected '%c'".formatted(invalid, expected), source, context);
    }

    public static OahuError invalidCharacterSequence(String invalid, String expected, Source source, Context context) {
        return forLexer("Character '%s' is invalid; expected '%s'".formatted(invalid, expected), source, context);
    }

    public static OahuError invalidNumber(String invalid, Source source, Context context) {
        return forLexer("Number '%s' is invalid".formatted(invalid), source, context);
    }

    public static OahuError invalidUnicode(String invalid, Source source, Context context) {
        return forLexer("Unicode hexcode '%s' is invalid".formatted(invalid), source, context);
    }

    private static OahuError forParser(String message, Source source, Context context) {
        return error(Stage.PARSER, message, source, context);
    }

    public static OahuError earlyElseBranch(Source source, Context context) {
        return forParser("Else branch must be the last branch", source, context);
    }

    public static OahuError invalidArgumentName(Source source, Context context) {
        return forParser("Argument name must be first variable name", source, context);
    }

    public static OahuError invalidTerminal(TokenType invalid, Source source, Context context) {
        return forParser("Terminal expression beginning with type '%s' is invalid".formatted(invalid), source, context);
    }

    public static OahuError invalidTokenType(TokenType invalid, TokenType expected, Source source, Context context) {
        return forParser("Token type '%s' is invalid; expected '%s'".formatted(invalid, expected), source, context);
    }

    public static OahuError reservedName(Expr.Name name, Source source, Context context) {
        return forParser("The name '%s' is reserved".formatted(name), source, context);
    }

    private static OahuError forScript(String message, Source source, Context context) {
        return error(Stage.RUNTIME, message, source, context);
    }

    public static OahuError invalidUnaryOperand(Object operand, Expr.Unary.Operator operator, Source source, Context context) {
        return forScript("Operand '%s' for unary %s operator is invalid".formatted(operand, operator), source, context);
    }

    public static OahuError invalidLeftOperand(Object operand, Expr.Binary.Operator operator, Source source, Context context) {
        return forScript("Left operand '%s' for binary %s operator is invalid".formatted(operand, operator), source, context);
    }

    public static OahuError invalidRightOperand(Object operand, Expr.Binary.Operator operator, Source source, Context context) {
        return forScript("Right operand '%s' for binary %s operator is invalid".formatted(operand, operator), source, context);
    }

    public static OahuError notCallableValueError(Object target, Source source, Context context) {
        return forScript(String.format("Value '%s' is not callable!", target), source, context);
    }

    public static OahuError notSpreadableValueError(Object target, Source source, Context context) {
        return forScript(String.format("Value '%s' is not spreadable!", target), source, context);
    }

    public static OahuError reassignedConstant(Source source, Context context) {
        return forScript("Constant cannot be reassigned", source, context);
    }

    public static OahuError redefinedName(Expr.Name name, Source source, Context context) {
        return forScript("Name '%s' has already been defined".formatted(name), source, context);
    }

    public static OahuError redefinedName(String name, Source source, Context context) {
        return forScript("Name '%s' has already been defined".formatted(name), source, context);
    }

    public static OahuError undefinedName(Expr.Name name, Source source, Context context) {
        return forScript("Name '%s' has not been defined".formatted(name.value()), source, context);
    }

    public static OahuError unhandledRedirect(Redirect redirect, Source source, Context context) {
        return forScript("%s was not handled".formatted(redirect), source, context);
    }

    private static OahuError forLinker(String message) {
        return error(message);
    }

    public static OahuError invalidFormatPositionError(String position) {
        return forLinker(STR."String format position '\{position}' must be first number!");
    }

    public static OahuError invalidLinkArgumentError(Object argument, String paramID) {
        return forLinker(STR."Argument '\{argument}' for link function parameter '\{paramID}' is invalid!");
    }

    public static OahuError invalidStringIndexError(double index) {
        return forLinker(STR."String index '\{(int) index}' out of bounds!");
    }

    public static OahuError linkResolutionError(String path, int expected, int received) {
        return forLinker(STR."Linked function '\{path}' expected '\{expected}' arguments; received '\{received}'!");
    }

    public static OahuError missingFunctionLink(String path) {
        return forLinker(STR."Link for function '\{path}' is unavailable!");
    }

    public static OahuError missingLink(Expr.Name name) {
        return forLinker(STR."Link for file '\{name.value()}' is unavailable!");
    }

    public static OahuError missingClassLinkError(String path) {
        return forLinker(STR."Link for class '\{path}' is unavailable!");
    }

    public static OahuError missingMemberError(String path) {
        return forLinker(STR."Instance member '\{path}' could not be linked!");
    }

    public static OahuError negativeIndexError() {
        return forLinker("Index cannot be negative!");
    }

    public static Error failure(String message) {
        return new Error(message);
    }
}
