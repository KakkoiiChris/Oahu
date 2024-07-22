package kakkoiichris.oahu.lexer;

public sealed interface TokenType {
    enum Keyword implements TokenType {
        AND("and"),
        OR("or"),
        LET("let"),
        VAR("var"),
        MUT("mut"),
        FUN("fun"),
        CLASS("class"),
        OBJECT("object"),
        ENUM("enum"),
        IF("if"),
        ELSE("else"),
        WHEN("when"),
        FOR("for"),
        WHILE("while"),
        DO("do"),
        LOOP("loop"),
        BREAK("break"),
        CONTINUE("continue"),
        RETURN("return"),
        THROW("throw"),
        TRY("try"),
        CATCH("catch"),
        FINALLY("finally"),
        EXIT("exit");

        private final String symbol;

        Keyword(final String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }

        @Override
        public String toString() {
            return symbol;
        }
    }

    enum Symbol implements TokenType {
        EQUAL("="),
        PLUS_EQUAL("+="),
        DASH_EQUAL("-="),
        STAR_EQUAL("*="),
        SLASH_EQUAL("/="),
        PERCENT_EQUAL("%="),
        DOUBLE_EQUAL("=="),
        EXCLAMATION_EQUAL("!="),
        LESS("<"),
        LESS_EQUAL("<="),
        GREATER(">"),
        GREATER_EQUAL(">="),
        PLUS("+"),
        DASH("-"),
        STAR("*"),
        SLASH("/"),
        PERCENT("%"),
        EXCLAMATION("!"),
        AT("@"),
        COMMA(","),
        LEFT_PAREN("("),
        RIGHT_PAREN(")"),
        LEFT_SQUARE("["),
        RIGHT_SQUARE("]"),
        LEFT_BRACE("{"),
        RIGHT_BRACE("}"),
        DOT("."),
        QUESTION("?"),
        DOUBLE_QUESTION("??"),
        COLON(":"),
        POUND("#"),
        ARROW("->");

        private final String symbol;

        Symbol(final String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }

        @Override
        public String toString() {
            return symbol;
        }
    }

    record Value(Object value) implements TokenType {
    }

    record Name(String name) implements TokenType {
    }

    final class EndOfLine implements TokenType {
        private static EndOfLine instance;

        private EndOfLine() {
        }

        public static EndOfLine get() {
            if (instance == null) {
                instance = new EndOfLine();
            }

            return instance;
        }
    }

    final class EndOfFile implements TokenType {
        private static EndOfFile instance;

        private EndOfFile() {
        }

        public static EndOfFile get() {
            if (instance == null) {
                instance = new EndOfFile();
            }

            return instance;
        }
    }
}