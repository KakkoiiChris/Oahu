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
import kakkoiichris.oahu.lexer.Lexer;
import kakkoiichris.oahu.lexer.Token;
import kakkoiichris.oahu.lexer.TokenType;
import kakkoiichris.oahu.util.OahuError;
import kakkoiichris.oahu.util.OahuWarning;
import kakkoiichris.oahu.util.Source;
import kakkoiichris.oahu.util.Util;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class Parser {
    private final Source source;
    private final Lexer lexer;

    private Token<?> currentToken;

    public Parser(Source source, Lexer lexer) {
        this.source = source;
        this.lexer = lexer;

        currentToken = lexer.next();
    }

    public Program parse() {
        var stmts = new ArrayList<Stmt>();

        while (!atEndOfFile()) {
            stmts.add(stmt());
        }

        return new Program(stmts);
    }

    private Context here() {
        return currentToken.context();
    }

    private void step() {
        if (lexer.hasNext()) {
            currentToken = lexer.next();
        }
    }

    private boolean match(TokenType type) {
        return currentToken.type() == type;
    }

    private boolean matchAny(TokenType... types) {
        for (var type : types) {
            if (match(type)) {
                return true;
            }
        }

        return false;
    }

    private <X extends TokenType> boolean match(Class<X> clazz) {
        return clazz.isInstance(currentToken.type());
    }

    private boolean skip(TokenType type) {
        if (match(type)) {
            step();

            return true;
        }

        return false;
    }

    private boolean skipLine(TokenType type) {
        if (match(type)) {
            step();

            newLine();

            return true;
        }

        return false;
    }

    private void mustSkip(TokenType type) {
        if (!skip(type)) {
            throw OahuError.invalidTokenType(currentToken.type(), type, source, here());
        }
    }

    private void mustSkipLine(TokenType type) {
        if (!skipLine(type)) {
            throw OahuError.invalidTokenType(currentToken.type(), type, source, here());
        }
    }

    private void newLine() {
        while (match(TokenType.EndOfLine.get())) {
            step();
        }
    }

    @SuppressWarnings("unchecked")
    private <X extends TokenType> Token<X> get() {
        var token = currentToken;

        mustSkip(token.type());

        return (Token<X>) token;
    }

    private boolean atEndOfFile() {
        return match(TokenType.EndOfFile.get());
    }

    private void validateName(Expr.Name name) {
        if (name.value().matches("base|this")) {
            throw OahuError.reservedName(name, source, name.context());
        }

        if (name.value().matches("_+")) {
            if (name.value().length() == 1) {
                OahuWarning.discardedName(source, name.context());
            }
            else {
                OahuWarning.vagueName(source, name.context());
            }
        }
    }

    private Stmt stmt() {
        if (matchAny(TokenType.Keyword.LET, TokenType.Keyword.VAR)) {
            return declarationStmt();
        }

        if (match(TokenType.Keyword.WHILE)) {
            return whileStmt();
        }

        if (match(TokenType.Keyword.DO)) {
            return doStmt();
        }

        if (match(TokenType.Keyword.LOOP)) {
            return loopStmt();
        }

        if (match(TokenType.Keyword.FOR)) {
            return forStmt();
        }

        if (match(TokenType.Keyword.BREAK)) {
            return breakStmt();
        }

        if (match(TokenType.Keyword.CONTINUE)) {
            return continueStmt();
        }

        if (match(TokenType.Keyword.CATCH)) {
            return throwStmt();
        }

        if (match(TokenType.Keyword.RETURN)) {
            return returnStmt();
        }

        if (match(TokenType.Keyword.EXIT)) {
            return exitStmt();
        }

        if (match(TokenType.Keyword.FUN)) {
            return funStmt();
        }

        if (match(TokenType.Keyword.CLASS)) {
            return classStmt();
        }

        if (match(TokenType.Keyword.ENUM)) {
            return enumStmt();
        }

        if (match(TokenType.Symbol.LEFT_BRACE)) {
            return stmtBody();
        }

        return expressionStmt();
    }

    private Stmt.Declaration declarationStmt() {
        var location = here();

        var constant = skip(TokenType.Keyword.LET);

        if (!constant) {
            mustSkip(TokenType.Keyword.VAR);
        }

        var mutable = skip(TokenType.Keyword.MUT);

        var names = new ArrayList<Expr.Name>();

        var destructured = skip(TokenType.Symbol.LEFT_PAREN);

        if (destructured) {
            do {
                var name = name();

                validateName(name);

                names.add(name);
            }
            while (skip(TokenType.Symbol.COMMA));

            mustSkip(TokenType.Symbol.RIGHT_PAREN);
        }
        else {
            names.add(name());
        }

        mustSkipLine(TokenType.Symbol.EQUAL);

        var expr = expr();

        newLine();

        return new Stmt.Declaration(location, constant, mutable, destructured, names, expr);
    }

    private Stmt stmtBody() {
        var location = here();

        if (skipLine(TokenType.Symbol.LEFT_BRACE)) {
            var stmts = new ArrayList<Stmt>();

            while (!skip(TokenType.Symbol.RIGHT_BRACE)) {
                stmts.add(stmt());

                newLine();
            }

            return new Stmt.Block(location, stmts);
        }

        return stmt();
    }

    private Stmt.While whileStmt() {
        var location = here();

        mustSkip(TokenType.Keyword.WHILE);

        var label = skip(TokenType.Symbol.AT) ? name() : Expr.Name.none();

        mustSkip(TokenType.Symbol.LEFT_PAREN);

        var condition = expr();

        mustSkip(TokenType.Symbol.RIGHT_PAREN);

        var body = stmtBody();

        return new Stmt.While(location, label, condition, body);
    }

    private Stmt.Do doStmt() {
        var location = here();

        mustSkip(TokenType.Keyword.DO);

        var label = skip(TokenType.Symbol.AT) ? name() : Expr.Name.none();

        var body = stmtBody();

        newLine();

        mustSkip(TokenType.Keyword.WHILE);

        mustSkip(TokenType.Symbol.LEFT_PAREN);

        var condition = expr();

        mustSkip(TokenType.Symbol.RIGHT_PAREN);

        return new Stmt.Do(location, label, body, condition);
    }

    private Stmt.Loop loopStmt() {
        var location = here();

        mustSkip(TokenType.Keyword.LOOP);

        var label = skip(TokenType.Symbol.AT) ? name() : Expr.Name.none();

        Expr count = Expr.Empty.get();

        if (skip(TokenType.Symbol.LEFT_PAREN)) {
            count = expr();

            mustSkip(TokenType.Symbol.RIGHT_PAREN);
        }

        var body = stmtBody();

        return new Stmt.Loop(location, label, count, body);
    }

    private Stmt.For forStmt() {
        var location = here();

        mustSkip(TokenType.Keyword.FOR);

        var label = skip(TokenType.Symbol.AT) ? name() : Expr.Name.none();

        mustSkip(TokenType.Symbol.LEFT_PAREN);

        var destructured = false;

        var names = new ArrayList<Expr.Name>();

        if (skip(TokenType.Symbol.LEFT_PAREN)) {
            destructured = true;

            do {
                names.add(name());
            }
            while (skip(TokenType.Symbol.COMMA));

            mustSkip(TokenType.Symbol.RIGHT_PAREN);
        }

        mustSkip(TokenType.Symbol.COLON);

        var expr = expr();

        mustSkip(TokenType.Symbol.RIGHT_PAREN);

        var body = stmtBody();

        return new Stmt.For(location, label, destructured, names, expr, body);
    }

    private Stmt.Break breakStmt() {
        var location = here();

        mustSkip(TokenType.Keyword.BREAK);

        var label = skip(TokenType.Symbol.AT) ? name() : Expr.Name.none();

        return new Stmt.Break(location, label);
    }

    private Stmt.Continue continueStmt() {
        var location = here();

        mustSkip(TokenType.Keyword.CONTINUE);

        var label = skip(TokenType.Symbol.AT) ? name() : Expr.Name.none();

        return new Stmt.Continue(location, label);
    }

    private Stmt.Throw throwStmt() {
        var location = here();

        mustSkip(TokenType.Keyword.THROW);

        var expr = expr();

        return new Stmt.Throw(location, expr);
    }

    private Stmt.Return returnStmt() {
        var location = here();

        mustSkip(TokenType.Keyword.RETURN);

        Expr expr = Expr.Empty.get();

        if (!match(TokenType.EndOfLine.get())) {
            expr = expr();
        }

        return new Stmt.Return(location, expr);
    }

    private Stmt.Exit exitStmt() {
        var location = here();

        mustSkip(TokenType.Keyword.EXIT);

        Expr expr = Expr.Empty.get();

        if (!match(TokenType.EndOfLine.get())) {
            expr = expr();
        }

        return new Stmt.Exit(location, expr);
    }

    private Stmt.Fun funStmt() {
        var location = here();

        mustSkip(TokenType.Keyword.FUN);

        var isLinked = skip(TokenType.Symbol.AT);

        var name = name();

        validateName(name);

        var params = new ArrayList<Stmt.Fun.Param>();

        if (skip(TokenType.Symbol.LEFT_PAREN) && !skip(TokenType.Symbol.RIGHT_PAREN)) {
            do {
                var paramLoc = here();

                var mutable = skip(TokenType.Keyword.MUT);

                var varargs = skip(TokenType.Symbol.STAR);

                if (mutable && varargs) {
                    throw OahuError.failure("NOT MUTABLE AND VARARGS");//TODO
                }

                var paramName = name();

                validateName(paramName);

                var defaultValue = skip(TokenType.Symbol.EQUAL) ? expr() : Expr.Empty.get();

                params.add(new Stmt.Fun.Param(paramLoc, mutable, varargs, paramName, defaultValue));
            }
            while (skip(TokenType.Symbol.COMMA));

            mustSkip(TokenType.Symbol.RIGHT_PAREN);
        }

        Stmt body = Stmt.Empty.get();

        if (match(TokenType.Symbol.LEFT_BRACE)) {
            body = stmtBody();
        }
        else if (skip(TokenType.Symbol.EQUAL)) {
            body = new Stmt.Return(here(), expr());
        }

        return new Stmt.Fun(location, "", isLinked, name, params, body);
    }

    private Stmt.Class classStmt() {
        return null;
    }

    private Stmt.Enum enumStmt() {
        return null;
    }

    private Stmt.Expression expressionStmt() {
        var location = here();

        var expr = expr();

        newLine();

        return new Stmt.Expression(location, expr);
    }

    private Expr expr() {
        return assignment();
    }

    private Expr assignment() {
        var expr = spread();

        if (matchAny(TokenType.Symbol.EQUAL, TokenType.Symbol.PLUS_EQUAL, TokenType.Symbol.DASH_EQUAL, TokenType.Symbol.STAR_EQUAL, TokenType.Symbol.SLASH_EQUAL, TokenType.Symbol.PERCENT_EQUAL)) {
            Token<TokenType.Symbol> op = get();

            return switch (op.type()) {
                case TokenType.Symbol.EQUAL -> {
                    var name = Util.cast(Expr.Name.class, expr)
                        .orElseThrow(); // TODO Assign Name

                    yield new Expr.Assign(op.context(), name, assignment());
                }

                case TokenType.Symbol.PLUS_EQUAL -> desugaredAssignment(op.context(), Expr.Binary.Operator.ADD, expr);

                case TokenType.Symbol.DASH_EQUAL ->
                    desugaredAssignment(op.context(), Expr.Binary.Operator.SUBTRACT, expr);

                case TokenType.Symbol.STAR_EQUAL ->
                    desugaredAssignment(op.context(), Expr.Binary.Operator.MULTIPLY, expr);

                case TokenType.Symbol.SLASH_EQUAL ->
                    desugaredAssignment(op.context(), Expr.Binary.Operator.DIVIDE, expr);

                case TokenType.Symbol.PERCENT_EQUAL ->
                    desugaredAssignment(op.context(), Expr.Binary.Operator.MODULUS, expr);

                default -> throw OahuError.failure("Broken assignment operator '%s'!".formatted(op.type()));
            };
        }

        return expr;
    }

    private Expr.Assign desugaredAssignment(Context context, Expr.Binary.Operator operator, Expr left) {
        var name = Util.cast(Expr.Name.class, left)
            .orElseThrow(); // TODO Assign Name

        var value = new Expr.Binary(context, operator, left, assignment());

        return new Expr.Assign(context, name, value);
    }

    private Expr subExpr() {
        return spread();
    }

    private Expr spread() {
        if (match(TokenType.Symbol.STAR)) {
            var op = currentToken;

            mustSkip(TokenType.Symbol.STAR);

            return new Expr.Unary(op.context(), Expr.Unary.Operator.get(op.type()), disjunction());
        }

        return disjunction();
    }

    private Expr disjunction() {
        var expr = conjunction();

        while (match(TokenType.Keyword.OR)) {
            var op = currentToken;

            mustSkip(op.type());

            expr = new Expr.Binary(op.context(), Expr.Binary.Operator.get(op.type()), expr, conjunction());
        }

        return expr;
    }

    private Expr conjunction() {
        var expr = equality();

        while (match(TokenType.Keyword.AND)) {
            var op = currentToken;

            mustSkip(op.type());

            expr = new Expr.Binary(op.context(), Expr.Binary.Operator.get(op.type()), expr, equality());
        }

        return expr;
    }

    private Expr equality() {
        var expr = comparison();

        while (matchAny(TokenType.Symbol.DOUBLE_EQUAL, TokenType.Symbol.EXCLAMATION_EQUAL)) {
            var op = currentToken;

            mustSkip(op.type());

            expr = new Expr.Binary(op.context(), Expr.Binary.Operator.get(op.type()), expr, comparison());
        }

        return expr;
    }

    private Expr comparison() {
        var expr = additive();

        while (matchAny(TokenType.Symbol.LESS, TokenType.Symbol.LESS_EQUAL, TokenType.Symbol.GREATER, TokenType.Symbol.GREATER_EQUAL)) {
            var op = currentToken;

            mustSkip(op.type());

            expr = new Expr.Binary(op.context(), Expr.Binary.Operator.get(op.type()), expr, additive());
        }

        return expr;
    }

    private Expr additive() {
        var expr = multiplicative();

        while (matchAny(TokenType.Symbol.PLUS, TokenType.Symbol.DASH)) {
            var op = currentToken;

            mustSkip(op.type());

            expr = new Expr.Binary(op.context(), Expr.Binary.Operator.get(op.type()), expr, multiplicative());
        }

        return expr;
    }

    private Expr multiplicative() {
        var expr = prefix();

        while (matchAny(TokenType.Symbol.STAR, TokenType.Symbol.SLASH, TokenType.Symbol.PERCENT)) {
            var op = currentToken;

            mustSkip(op.type());

            expr = new Expr.Binary(op.context(), Expr.Binary.Operator.get(op.type()), expr, prefix());
        }

        return expr;
    }

    private Expr prefix() {
        if (matchAny(TokenType.Symbol.DASH, TokenType.Symbol.EXCLAMATION, TokenType.Symbol.POUND)) {
            var op = currentToken;

            mustSkip(op.type());

            return new Expr.Unary(op.context(), Expr.Unary.Operator.get(op.type()), prefix());
        }

        return postfix();
    }

    private Expr postfix() {
        var expr = terminal();

        while (matchAny(TokenType.Symbol.DOT, TokenType.Symbol.LEFT_SQUARE, TokenType.Symbol.LEFT_PAREN)) {
            var op = currentToken;

            if (skip(TokenType.Symbol.DOT)) {
                expr = new Expr.Binary(op.context(), Expr.Binary.Operator.get(op.type()), expr, terminal());
            }
            else if (skip(TokenType.Symbol.LEFT_SQUARE)) {
                var args = new ArrayList<Expr.Invoke.Arg>();

                if (!skip(TokenType.Symbol.RIGHT_SQUARE)) {
                    do {
                        var argLoc = here();

                        var spread = skip(TokenType.Symbol.STAR);

                        var argExpr = expr();

                        args.add(new Expr.Invoke.Arg(argLoc, spread, Expr.Name.none(), argExpr));
                    }
                    while (skip(TokenType.Symbol.COMMA));

                    mustSkip(TokenType.Symbol.RIGHT_SQUARE);
                }

                expr = new Expr.Index(op.context(), expr, args);
            }
            else if (skip(TokenType.Symbol.LEFT_PAREN)) {
                var args = new ArrayList<Expr.Invoke.Arg>();

                if (!skip(TokenType.Symbol.RIGHT_PAREN)) {
                    do {
                        var argLoc = here();

                        var spread = skip(TokenType.Symbol.STAR);

                        var argName = Expr.Name.none();

                        var argExpr = subExpr();

                        if (skip(TokenType.Symbol.EQUAL)) {
                            if (argExpr instanceof Expr.Name name) {
                                argName = name;

                                argExpr = subExpr();
                            }
                            else {
                                throw OahuError.invalidArgumentName(source, argExpr.context());
                            }
                        }

                        args.add(new Expr.Invoke.Arg(argLoc, spread, argName, argExpr));
                    }
                    while (skip(TokenType.Symbol.COMMA));

                    mustSkip(TokenType.Symbol.RIGHT_PAREN);
                }

                expr = new Expr.Invoke(op.context(), expr, args);
            }
        }

        return expr;
    }

    private Expr terminal() {
        if (match(TokenType.Value.class)) {
            return value();
        }

        if (match(TokenType.Name.class)) {
            return name();
        }

        if (match(TokenType.Symbol.LEFT_PAREN)) {
            return nested();
        }

        if (match(TokenType.Symbol.LEFT_SQUARE)) {
            return list();
        }

        if (match(TokenType.Symbol.COLON)) {
            return lambda();
        }

        if (match(TokenType.Keyword.IF)) {
            return ifExpr();
        }

        if (match(TokenType.Keyword.WHEN)) {
            return whenExpr();
        }

        if (match(TokenType.Keyword.TRY)) {
            return tryExpr();
        }

        throw OahuError.invalidTerminal(currentToken.type(), source, here());
    }

    private Expr.Value value() {
        Token<TokenType.Value> token = get();

        return new Expr.Value(token.context(), token.type().value());
    }

    private Expr.Name name() {
        Token<TokenType.Name> token = get();

        return new Expr.Name(token.context(), token.type().name());
    }

    private Expr nested() {
        mustSkip(TokenType.Symbol.LEFT_PAREN);

        var expr = expr();

        mustSkip(TokenType.Symbol.RIGHT_PAREN);

        return expr;
    }

    private Expr list() {
        var location = here();

        mustSkipLine(TokenType.Symbol.LEFT_SQUARE);

        var elements = new ArrayList<Expr>();

        var generator = false;

        if (match(TokenType.Symbol.RIGHT_SQUARE)) {
            do {
                elements.add(expr());
            }
            while (skipLine(TokenType.Symbol.COMMA));

            newLine();

            if (match(TokenType.Keyword.FOR)) {
                generator = true;
            }
        }

        if (generator) {
            var element = elements.getFirst();

            mustSkip(TokenType.Keyword.FOR);

            var names = new ArrayList<Expr.Name>();

            var destructured = false;

            if (skip(TokenType.Symbol.LEFT_PAREN)) {
                destructured = true;

                do {
                    names.add(name());
                }
                while (skip(TokenType.Symbol.COMMA));

                mustSkip(TokenType.Symbol.RIGHT_PAREN);
            }
            else {
                names.add(name());
            }

            mustSkip(TokenType.Symbol.COLON);

            var iterable = expr();

            newLine();

            var condition = skip(TokenType.Keyword.IF) ? expr() : Expr.Empty.get();

            newLine();

            mustSkip(TokenType.Symbol.RIGHT_SQUARE);

            return new Expr.ListFor(location, element, names, destructured, iterable, condition);
        }

        mustSkip(TokenType.Symbol.RIGHT_SQUARE);

        return new Expr.ListLiteral(location, elements);
    }

    private Expr exprBody() {
        var location = here();

        if (skipLine(TokenType.Symbol.LEFT_BRACE)) {
            var exprs = new ArrayList<Expr>();

            while (!skip(TokenType.Symbol.RIGHT_BRACE)) {
                exprs.add(exprBodyStmt());

                newLine();
            }

            return new Expr.Block(location, exprs);
        }

        return exprBodyStmt();
    }

    private Expr exprBodyStmt() {
        if (matchAny(
            TokenType.Keyword.LET,
            TokenType.Keyword.VAR,
            TokenType.Keyword.WHILE,
            TokenType.Keyword.DO,
            TokenType.Keyword.LOOP,
            TokenType.Keyword.FOR,
            TokenType.Keyword.BREAK,
            TokenType.Keyword.CONTINUE,
            TokenType.Keyword.RETURN,
            TokenType.Keyword.THROW,
            TokenType.Keyword.EXIT)
        ) {
            return new Expr.Statement(here(), stmt());
        }

        return expr();
    }

    private Expr.Lambda lambda() {
        var location = here();

        mustSkip(TokenType.Symbol.COLON);

        var names = new ArrayList<Expr.Name>();

        if (!skip(TokenType.Symbol.COLON)) {
            do {
                names.add(name());
            }
            while (skip(TokenType.Symbol.COMMA));

            mustSkip(TokenType.Symbol.ARROW);
        }

        var params = names
            .stream()
            .map(name -> new Stmt.Fun.Param(name.context(), false, false, name, null))
            .collect(Collectors.toList());

        var body = exprBody();

        var ret = new Stmt.Return(body.context(), body);

        var fun = new Stmt.Fun(location, "", false, Expr.Name.none(), params, ret);

        return new Expr.Lambda(location, fun);
    }

    private Expr ifExpr() {
        var location = here();

        mustSkip(TokenType.Keyword.IF);

        mustSkip(TokenType.Symbol.LEFT_PAREN);

        var condition = expr();

        mustSkip(TokenType.Symbol.RIGHT_PAREN);

        var body = exprBody();

        newLine();

        var elze = skip(TokenType.Keyword.ELSE) ? exprBody() : Expr.Empty.get();

        return new Expr.If(location, condition, body, elze);
    }

    private Expr.When whenExpr() {
        var location = here();

        mustSkip(TokenType.Keyword.WHEN);

        var subject = Expr.Name.none();

        if (skip(TokenType.Symbol.LEFT_PAREN)) {
            subject = name();

            mustSkip(TokenType.Symbol.RIGHT_PAREN);
        }

        mustSkipLine(TokenType.Symbol.LEFT_BRACE);

        var branches = new ArrayList<Expr.When.Branch>();

        Expr elze = Expr.Empty.get();

        do {
            if (skip(TokenType.Keyword.ELSE)) {
                mustSkip(TokenType.Symbol.ARROW);

                elze = exprBody();

                break;
            }

            var branchLocation = here();

            var condition = expr();

            mustSkipLine(TokenType.Symbol.ARROW);

            var body = exprBody();

            branches.add(new Expr.When.Branch(branchLocation, condition, body));

            newLine();
        }
        while (!match(TokenType.Symbol.RIGHT_BRACE));

        if (!skip(TokenType.Symbol.RIGHT_BRACE)) {
            throw OahuError.earlyElseBranch(source, here());
        }

        return new Expr.When(location, subject, branches, elze);
    }

    private Expr tryExpr() {
        var location = here();

        mustSkip(TokenType.Keyword.TRY);

        var body = exprBody();

        newLine();

        var error = Expr.Name.none();
        Expr catchBody = Expr.Empty.get();

        if (skip(TokenType.Keyword.CATCH)) {
            if (skip(TokenType.Symbol.LEFT_PAREN)) {
                error = name();

                mustSkip(TokenType.Symbol.RIGHT_PAREN);
            }

            catchBody = exprBody();
        }

        newLine();

        Expr finallyBody = Expr.Empty.get();

        if (skip(TokenType.Keyword.FINALLY)) {
            finallyBody = exprBody();
        }

        return new Expr.Try(location, body, error, catchBody, finallyBody);
    }
}
