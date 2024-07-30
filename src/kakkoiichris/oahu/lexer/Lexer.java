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

import kakkoiichris.oahu.runtime.data.Null;
import kakkoiichris.oahu.runtime.data.Unit;
import kakkoiichris.oahu.util.OahuError;
import kakkoiichris.oahu.util.Source;
import kakkoiichris.oahu.util.Util;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Lexer implements Iterator<Token<?>> {
    private final Source source;

    private final Map<String, Object> literals =
        Stream.of(true, false, Null.get(), Unit.get())
            .collect(Collectors.toMap(Objects::toString, Function.identity()));

    private int pos = 0, row = 1, column = 1;

    public Lexer(Source source) {
        this.source = source;
    }

    @Override
    public boolean hasNext() {
        return pos <= source.text().length();
    }

    @Override
    public Token<?> next() {
        while (!atEndOfFile()) {
            if (match(Lexer::isHorizontalWhitespace)) {
                skipWhitespace();
                continue;
            }

            if (match("//")) {
                skipLineComment();
                continue;
            }

            if (match("/*")) {
                skipBlockComment();
                continue;
            }

            if (match(Character::isDigit)) {
                return number();
            }

            if (match(Lexer::isIdentifierStart)) {
                return word();
            }

            if (match('"')) {
                return string();
            }

            return operator();
        }

        return new Token<>(here(), TokenType.EndOfFile.get());
    }

    private Context here() {
        return new Context(source.name(), row, column, 1);
    }

    private void step(int offset) {
        for (var i = 0; i < offset; i++) {
            if (match('\n')) {
                row++;
                column = 1;
            }
            else {
                column++;
            }

            pos++;
        }
    }

    private void step() {
        step(1);
    }

    private char peek(int offset) {
        var index = pos + offset;

        if (index >= source.text().length()) {
            return '\0';
        }

        return source.text().charAt(index);
    }

    private char peek() {
        return peek(0);
    }

    private String look(int length) {
        var result = new StringBuilder();

        for (var i = 0; i < length; i++) {
            result.append(peek(i));
        }

        return result.toString();
    }

    private boolean match(char c) {
        return peek() == c;
    }

    private boolean match(Predicate<Character> p) {
        return p.test(peek());
    }

    private boolean matchNext(Predicate<Character> p) {
        return p.test(peek(1));
    }

    private boolean match(String s) {
        return look(s.length()).equals(s);
    }

    private boolean matchAny(char... chars) {
        for (var c : chars) {
            if (match(c)) {
                return true;
            }
        }

        return false;
    }

    private boolean skip(char c) {
        if (match(c)) {
            step();

            return true;
        }

        return false;
    }

    private boolean skip(Predicate<Character> p) {
        if (match(p)) {
            step();

            return true;
        }

        return false;
    }

    private boolean skip(String s) {
        if (match(s)) {
            step(s.length());

            return true;
        }

        return false;
    }

    private boolean skipAny(char... chars) {
        for (var c : chars) {
            if (skip(c)) {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("SameParameterValue")
    private void mustSkip(char c) {
        if (!skip(c)) {
            throw OahuError.invalidCharacter(c, peek(), source, here());
        }
    }

    private void mustSkip(String s) {
        if (!skip(s)) {
            throw OahuError.invalidCharacterSequence(look(s.length()), s, source, here());
        }
    }

    private boolean atEndOfFile() {
        return match('\0');
    }

    private static boolean isHorizontalWhitespace(char c) {
        return c == ' ' || c == '\t';
    }

    private static boolean isIdentifierStart(char c) {
        return Character.isLetter(c) || c == '_';
    }

    private static boolean isIdentifier(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private static boolean isBinaryDigit(char c) {
        return "01_".indexOf(c) != -1;
    }

    private static boolean isHexadecimalDigit(char c) {
        return "0123456789ABCDEFabcdef_".indexOf(c) != -1;
    }

    private static boolean isDecimalDigit(char c) {
        return "0123456789_".indexOf(c) != -1;
    }

    private void skipWhitespace() {
        while (match(Lexer::isHorizontalWhitespace)) {
            step();
        }
    }

    private void skipLineComment() {
        mustSkip("//");

        do {
            step();
        }
        while (!(atEndOfFile() || skip('\n')));
    }

    private void skipBlockComment() {
        mustSkip("/*");

        while (!skip("*/")) {
            if (atEndOfFile()) {
                throw OahuError.earlyEndOfFile(source, here());
            }

            step();
        }
    }

    private void take(StringBuilder result) {
        result.append(peek());

        step();
    }

    private Token<TokenType.Value> number() {
        if (match("0b")) {
            return binary();
        }

        if (match("0x")) {
            return hexadecimal();
        }

        return decimal();
    }

    private Token<TokenType.Value> binary() {
        var start = here();

        mustSkip("0b");

        var result = new StringBuilder();

        do {
            take(result);
        }
        while (match(Lexer::isBinaryDigit));

        var context = start.rangeTo(here());

        var value = Util
            .parseInt(result.toString(), 2)
            .map(Integer::doubleValue)
            .orElseThrow(() -> OahuError.invalidNumber(result.toString(), source, context));

        return new Token<>(context, new TokenType.Value(value));
    }

    private Token<TokenType.Value> hexadecimal() {
        var start = here();

        mustSkip("0x");

        var result = new StringBuilder();

        do {
            take(result);
        }
        while (match(Lexer::isHexadecimalDigit));

        var context = start.rangeTo(here());

        var value = Util
            .parseInt(result.toString(), 16)
            .map(Integer::doubleValue)
            .orElseThrow(() -> OahuError.invalidNumber(result.toString(), source, context));

        return new Token<>(context, new TokenType.Value(value));
    }

    private Token<TokenType.Value> decimal() {
        var start = here();

        var result = new StringBuilder();

        do {
            take(result);
        }
        while (match(Lexer::isDecimalDigit));

        if (match('.') && matchNext(Lexer::isDecimalDigit)) {
            do {
                take(result);
            }
            while (match(Lexer::isDecimalDigit));
        }

        if (matchAny('E', 'e')) {
            take(result);

            do {
                take(result);
            }
            while (match(Lexer::isDecimalDigit));
        }

        var context = start.rangeTo(here());

        var value = Util
            .parseNumber(result.toString())
            .orElseThrow(() -> OahuError.invalidNumber(result.toString(), source, context));

        return new Token<>(context, new TokenType.Value(value));
    }

    private Token<?> word() {
        var start = here();

        var result = new StringBuilder();

        do {
            take(result);
        }
        while (match(Lexer::isIdentifier));

        var context = start.rangeTo(here());

        var keyword = Util.getEntry(TokenType.Keyword.class, result.toString().toUpperCase());

        if (keyword.isPresent()) {
            return new Token<>(context, keyword.get());
        }

        var literal = literals.get(result.toString());

        if (literal != null) {
            return new Token<>(context, new TokenType.Value(literal));
        }

        return new Token<>(context, new TokenType.Name(result.toString()));
    }

    private char unicode(int size) {
        var start = here();

        var result = new StringBuilder();

        for (var i = 0; i < size; i++) {
            take(result);
        }

        var context = start.rangeTo(here());

        var value = Util
            .parseInt(result.toString(), 16)
            .orElseThrow(() -> OahuError.invalidUnicode(result.toString(), source, context));

        return (char) value.intValue();
    }

    private Token<TokenType.Value> string() {
        var start = here();

        var result = new StringBuilder();

        mustSkip('"');

        while (!skip('"')) {
            if (atEndOfFile()) {
                throw OahuError.earlyEndOfFile(source, here());
            }

            if (skip('\\')) {
                if (skip('\\')) {
                    result.append('\\');
                }
                else if (skip('"')) {
                    result.append('"');
                }
                else if (skip('0')) {
                    result.append('\0');
                }
                else if (skip('b')) {
                    result.append('\b');
                }
                else if (skip('n')) {
                    result.append('\n');
                }
                else if (skip('r')) {
                    result.append('\r');
                }
                else if (skip('t')) {
                    result.append('\t');
                }
                else if (skip('x')) {
                    result.append(unicode(2));
                }
                else if (skip('u')) {
                    result.append(unicode(4));
                }
                else if (skip('U')) {
                    result.append(unicode(8));
                }
                else if (skip('(')) {
                    var name = new StringBuilder();

                    while (!skip(')')) {
                        take(name);
                    }

                    result.append((char) Character.codePointOf(name.toString()));
                }
                else {
                    throw OahuError.illegalCharacterEscape(peek(), source, here());
                }
            }
            else {
                take(result);
            }
        }

        var context = start.rangeTo(here());

        return new Token<>(context, new TokenType.Value(result.toString()));
    }

    private Token<?> operator() {
        var start = here();

        TokenType type;

        if (skip('+')) {
            type = skip('=') ? TokenType.Symbol.PLUS_EQUAL : TokenType.Symbol.PLUS;
        }
        else if (skip('-')) {
            if (skip('=')) {
                type = TokenType.Symbol.DASH_EQUAL;
            }
            else if (skip('>')) {
                type = TokenType.Symbol.ARROW;
            }
            else {
                type = TokenType.Symbol.DASH;
            }
        }
        else if (skip('*')) {
            type = skip('=') ? TokenType.Symbol.STAR_EQUAL : TokenType.Symbol.STAR;
        }
        else if (skip('/')) {
            type = skip('=') ? TokenType.Symbol.SLASH_EQUAL : TokenType.Symbol.SLASH;
        }
        else if (skip('%')) {
            type = skip('=') ? TokenType.Symbol.PERCENT_EQUAL : TokenType.Symbol.PERCENT;
        }
        else if (skip('<')) {
            type = skip('=') ? TokenType.Symbol.LESS_EQUAL : TokenType.Symbol.LESS;
        }
        else if (skip('>')) {
            type = skip('=') ? TokenType.Symbol.GREATER_EQUAL : TokenType.Symbol.GREATER;
        }
        else if (skip('=')) {
            type = skip('=') ? TokenType.Symbol.DOUBLE_EQUAL : TokenType.Symbol.EQUAL;
        }
        else if (skip('!')) {
            type = skip('=') ? TokenType.Symbol.EXCLAMATION_EQUAL : TokenType.Symbol.EXCLAMATION;
        }
        else if (skip('@')) {
            type = TokenType.Symbol.AT;
        }
        else if (skip('(')) {
            type = TokenType.Symbol.LEFT_PAREN;
        }
        else if (skip(')')) {
            type = TokenType.Symbol.RIGHT_PAREN;
        }
        else if (skip('[')) {
            type = TokenType.Symbol.LEFT_SQUARE;
        }
        else if (skip(']')) {
            type = TokenType.Symbol.RIGHT_SQUARE;
        }
        else if (skip('{')) {
            type = TokenType.Symbol.LEFT_BRACE;
        }
        else if (skip('}')) {
            type = TokenType.Symbol.RIGHT_BRACE;
        }
        else if (skip('.')) {
            type = TokenType.Symbol.DOT;
        }
        else if (skip('?')) {
            type = skip('?') ? TokenType.Symbol.DOUBLE_QUESTION : TokenType.Symbol.QUESTION;
        }
        else if (skip(',')) {
            type = TokenType.Symbol.COMMA;
        }
        else if (skip(':')) {
            type = TokenType.Symbol.COLON;
        }
        else if (skip('#')) {
            type = TokenType.Symbol.POUND;
        }
        else if (skipAny(';', '\r', '\n')) {
            type = TokenType.EndOfLine.get();
        }
        else {
            throw OahuError.illegalCharacter(peek(), source, here());
        }

        var context = start.rangeTo(here());

        return new Token<>(context, type);
    }
}
