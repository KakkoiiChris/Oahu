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
package kakkoiichris.oahu.parser;

import kakkoiichris.oahu.lexer.Context;
import kakkoiichris.oahu.lexer.TokenType;

import java.util.Arrays;
import java.util.List;

public sealed interface Expr {
    Context context();

    <X> X accept(Visitor<X> visitor);

    static Expr toExpr(Object x) {
        if (x instanceof Expr e) {
            return e;
        }

        return new Value(Context.none(), x);
    }

    interface Visitor<X> {
        default X visit(Expr expr) {
            return expr.accept(this);
        }

        X visitEmptyExpr(Empty expr);

        X visitValueExpr(Value expr);

        X visitNameExpr(Name expr);

        X visitUnaryExpr(Unary expr);

        X visitBinaryExpr(Binary expr);

        X visitAssignExpr(Assign expr);

        X visitIndexExpr(Index expr);

        X visitInvokeExpr(Invoke expr);

        X visitListLiteralExpr(ListLiteral expr);

        X visitListForExpr(ListFor expr);

        X visitLambdaExpr(Lambda expr);

        X visitBlockExpr(Block expr);

        X visitIfExpr(If expr);

        X visitWhenExpr(When expr);

        X visitTryExpr(Try expr);

        X visitStatementExpr(Statement expr);
    }

    final class Empty implements Expr {
        private static Empty instance;

        private Empty() {
        }

        public static Empty get() {
            if (instance == null) {
                instance = new Empty();
            }

            return instance;
        }

        @Override
        public Context context() {
            return Context.none();
        }

        @Override
        public <X> X accept(Visitor<X> visitor) {
            return visitor.visitEmptyExpr(this);
        }
    }

    record Value(Context context, Object value) implements Expr {
        @Override
        public <X> X accept(Visitor<X> visitor) {
            return visitor.visitValueExpr(this);
        }
    }

    record Name(Context context, String value) implements Expr {
        public static Name none() {
            return new Name(Context.none(), "");
        }

        public boolean isEmpty() {
            return value.isEmpty();
        }

        @Override
        public <X> X accept(Visitor<X> visitor) {
            return visitor.visitNameExpr(this);
        }
    }

    record Unary(Context context, Operator operator, Expr expr) implements Expr {
        public enum Operator {
            SPREAD(TokenType.Symbol.STAR),
            NEGATIVE(TokenType.Symbol.DASH),
            NOT(TokenType.Symbol.EXCLAMATION),
            SIZE(TokenType.Symbol.POUND);

            private final TokenType type;

            Operator(TokenType type) {
                this.type = type;
            }

            public static Operator get(TokenType type) {
                return Arrays
                    .stream(values())
                    .filter(o -> o.type == type)
                    .toList()
                    .getFirst();
            }
        }

        @Override
        public <X> X accept(Visitor<X> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    record Binary(Context context, Operator operator, Expr left, Expr right) implements Expr {
        public enum Operator {
            OR(TokenType.Keyword.OR),
            AND(TokenType.Keyword.AND),
            EQUAL(TokenType.Symbol.DOUBLE_EQUAL),
            NOT_EQUAL(TokenType.Symbol.EXCLAMATION_EQUAL),
            LESS(TokenType.Symbol.LESS),
            LESS_EQUAL(TokenType.Symbol.LESS_EQUAL),
            GREATER(TokenType.Symbol.GREATER),
            GREATER_EQUAL(TokenType.Symbol.GREATER_EQUAL),
            ADD(TokenType.Symbol.PLUS),
            SUBTRACT(TokenType.Symbol.DASH),
            MULTIPLY(TokenType.Symbol.STAR),
            DIVIDE(TokenType.Symbol.SLASH),
            MODULUS(TokenType.Symbol.PERCENT);

            private final TokenType type;

            Operator(TokenType type) {
                this.type = type;
            }

            public static Operator get(TokenType type) {
                return Arrays
                    .stream(values())
                    .filter(o -> o.type == type)
                    .toList()
                    .getFirst();
            }
        }

        @Override
        public <X> X accept(Visitor<X> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    record Assign(Context context, Name name, Expr value) implements Expr {
        @Override
        public <X> X accept(Visitor<X> visitor) {
            return visitor.visitAssignExpr(this);
        }
    }

    record Index(Context context, Expr target, List<Invoke.Arg> args) implements Expr {
        @Override
        public <X> X accept(Visitor<X> visitor) {
            return visitor.visitIndexExpr(this);
        }

        @Override
        public String toString() {
            return "x[y]";
        }
    }

    record Invoke(Context context, Expr target, List<Arg> args) implements Expr {
        @Override
        public <X> X accept(Visitor<X> visitor) {
            return visitor.visitInvokeExpr(this);
        }

        @Override
        public String toString() {
            return "x(y)";
        }

        public record Arg(Context context, boolean spread, Name name, Expr expr) {
            public boolean isPositional() {
                return name.isEmpty();
            }
        }

        public record Vararg(List<Expr> exprs) {
            public Vararg() {
                this(List.of());
            }
        }

        public record Spread(Expr expr) {
        }
    }

    record ListLiteral(Context context, List<Expr> elements) implements Expr {
        @Override
        public <X> X accept(Visitor<X> visitor) {
            return visitor.visitListLiteralExpr(this);
        }

        @Override
        public String toString() {
            return "[]";
        }
    }

    record ListFor(Context context, Expr element, List<Name> names, boolean destructured, Expr iterable,
                   Expr test) implements Expr {
        @Override
        public <X> X accept(Visitor<X> visitor) {
            return visitor.visitListForExpr(this);
        }

        @Override
        public String toString() {
            return "[for]";
        }
    }

    record Lambda(Context context, Stmt.Fun fun) implements Expr {
        @Override
        public <X> X accept(Visitor<X> visitor) {
            return visitor.visitLambdaExpr(this);
        }

        @Override
        public String toString() {
            return "fun";
        }
    }

    record Block(Context context, List<Expr> exprs) implements Expr {
        @Override
        public <X> X accept(Visitor<X> visitor) {
            return visitor.visitBlockExpr(this);
        }

        @Override
        public String toString() {
            return "{ ... }";
        }
    }

    record If(Context context, Expr condition, Expr body, Expr elze) implements Expr {
        @Override
        public <X> X accept(Visitor<X> visitor) {
            return visitor.visitIfExpr(this);
        }

        @Override
        public String toString() {
            return "if";
        }
    }

    record When(Context context, List<Branch> branches, Expr elze) implements Expr {
        @Override
        public <X> X accept(Visitor<X> visitor) {
            return visitor.visitWhenExpr(this);
        }

        @Override
        public String toString() {
            return "when";
        }

        public record Branch(Context context, Expr condition, Expr body) {
        }
    }

    record Try(Context context, Expr body, Name error, Expr catchBody, Expr finallyBody) implements Expr {
        @Override
        public <X> X accept(Visitor<X> visitor) {
            return visitor.visitTryExpr(this);
        }

        @Override
        public String toString() {
            return "try";
        }
    }

    record Statement(Context context, Stmt stmt) implements Expr {
        @Override
        public <X> X accept(Visitor<X> visitor) {
            return visitor.visitStatementExpr(this);
        }

        @Override
        public String toString() {
            return "statement<%s>".formatted(stmt);
        }
    }
}
