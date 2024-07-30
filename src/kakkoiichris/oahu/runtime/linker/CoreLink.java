package kakkoiichris.oahu.runtime.linker;

import kakkoiichris.oahu.parser.Stmt;
import kakkoiichris.oahu.runtime.data.Table;
import kakkoiichris.oahu.runtime.data.Unit;
import kakkoiichris.oahu.util.Console;
import kakkoiichris.oahu.util.OahuError;
import kakkoiichris.oahu.util.Source;
import kakkoiichris.oahu.util.Util;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Scanner;
import java.util.function.BiConsumer;

public class CoreLink implements Link {
    private static CoreLink instance;

    private final Scanner input = new Scanner(System.in);

    public static CoreLink get() {
        if (instance == null) {
            instance = new CoreLink();
        }

        return instance;
    }

    @Override
    public String getName() {
        return "core";
    }

    @Override
    public Source getSource() {
        return Source.ofResource("/core.apx");
    }

    @Override
    public void addFunctions(BiConsumer<String, Stmt.Fun.Link> addFunction) {
        linkGlobal(addFunction);
        linkAny(addFunction);
        linkBoolean(addFunction);
        linkNumber(addFunction);
        linkString(addFunction);
        linkList(addFunction);
        linkStringBuilder(addFunction);
    }

    private void linkGlobal(BiConsumer<String, Stmt.Fun.Link> addFunction) {
        addFunction.accept(".range", new Stmt.Fun.Link(3, (_, data) -> {
            var args = data.unwrap();

            var start = Util.cast(Double.class, args.get(0)).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.getFirst(), ".range@start"));
            var end = Util.cast(Double.class, args.get(1)).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.get(1), ".range@end"));
            var step = Util.cast(Double.class, args.get(2)).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.get(2), ".range@step"));

            var table = Table.empty();

            var i = start;

            while (i < end) {
                //list.push(script.toInstance(i));

                i += step;
            }

            return table;
        }));

        addFunction.accept(".read", new Stmt.Fun.Link((_, _) -> input.next()));

        addFunction.accept(".readln", new Stmt.Fun.Link((_, _) -> input.nextLine()));

        addFunction.accept(".print", new Stmt.Fun.Link(1, (_, data) -> {
            System.out.print(data.args().getFirst());

            return Unit.get();
        }));

        addFunction.accept(".println", new Stmt.Fun.Link(1, (_, data) -> {
            System.out.println(data.args().getFirst());

            return Unit.get();
        }));

        addFunction.accept(".time", new Stmt.Fun.Link((_, _) -> System.nanoTime() / 1e9));

        addFunction.accept(".wait", new Stmt.Fun.Link(1, (_, data) -> {
            var args = data.unwrap();

            var seconds = Util.cast(Double.class, args.getFirst()).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.getFirst(), ".wait@seconds"));

            try {
                Thread.sleep((long) (seconds * 1000));
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }

            return Unit.get();
        }));
    }

    private void linkAny(BiConsumer<String, Stmt.Fun.Link> addFunction) {
        addFunction.accept("Any.className", new Stmt.Fun.Link((_, data) -> data.instance().getClazz().name().value()));
    }

    private void linkBoolean(BiConsumer<String, Stmt.Fun.Link> addFunction) {
        addFunction.accept("Boolean.not", new Stmt.Fun.Link((_, data) -> {
            var thiz = data.instance().asBoolean().orElseThrow(() -> OahuError.failure("BOOLEAN_PRIMITIVE_MISMATCH"));

            return !thiz;
        }));

        addFunction.accept("Boolean.and", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asBoolean().orElseThrow(() -> OahuError.failure("BOOLEAN_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            var that = args.getFirst();

            if (that instanceof Boolean thatB) {
                return thiz && thatB;
            }
            else if (that instanceof String thatS) {
                return thiz.toString() + thatS;
            }

            throw OahuError.invalidLinkArgumentError(that, "Boolean.and@that");
        }));

        addFunction.accept("Boolean.xor", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asBoolean().orElseThrow(() -> OahuError.failure("BOOLEAN_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            var that = Util.cast(Boolean.class, args.getFirst()).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.getFirst(), "Boolean.xor@that"));

            return thiz ^ that;
        }));

        addFunction.accept("Boolean.or", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asBoolean().orElseThrow(() -> OahuError.failure("BOOLEAN_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            var that = Util.cast(Boolean.class, args.getFirst()).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.getFirst(), "Boolean.or@that"));

            return thiz || that;
        }));

        addFunction.accept("Boolean.equ", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asBoolean().orElseThrow(() -> OahuError.failure("BOOLEAN_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            if (args.getFirst() instanceof Boolean thatB) {
                return thiz == thatB;
            }

            return false;
        }));

        addFunction.accept("Boolean.toNumber", new Stmt.Fun.Link((_, data) -> {
            var thiz = data.instance().asBoolean().orElseThrow(() -> OahuError.failure("BOOLEAN_PRIMITIVE_MISMATCH"));

            return thiz ? 1.0 : 0.0;
        }));

        addFunction.accept("Boolean.toString", new Stmt.Fun.Link((_, data) -> {
            var thiz = data.instance().asBoolean().orElseThrow(() -> OahuError.failure("BOOLEAN_PRIMITIVE_MISMATCH"));

            return thiz.toString();
        }));
    }

    private void linkNumber(BiConsumer<String, Stmt.Fun.Link> addFunction) {
        addFunction.accept("Number.add", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asNumber().orElseThrow(() -> OahuError.failure("NUMBER_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            var that = Util.cast(Double.class, args.getFirst()).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.getFirst(), "Number.add@that"));

            return thiz + that;
        }));

        addFunction.accept("Number.sub", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asNumber().orElseThrow(() -> OahuError.failure("NUMBER_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            var that = Util.cast(Double.class, args.getFirst()).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.getFirst(), "Number.sub@that"));

            return thiz - that;
        }));

        addFunction.accept("Number.mul", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asNumber().orElseThrow(() -> OahuError.failure("NUMBER_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            var that = Util.cast(Double.class, args.getFirst()).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.getFirst(), "Number.mul@that"));

            return thiz * that;
        }));

        addFunction.accept("Number.div", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asNumber().orElseThrow(() -> OahuError.failure("NUMBER_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            var that = Util.cast(Double.class, args.getFirst()).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.getFirst(), "Number.div@that"));

            return (that == 0.0) ? Double.NaN : thiz / that;
        }));

        addFunction.accept("Number.rem", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asNumber().orElseThrow(() -> OahuError.failure("NUMBER_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            var that = Util.cast(Double.class, args.getFirst()).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.getFirst(), "Number.rem@that"));

            return (that == 0.0) ? Double.NaN : thiz % that;
        }));

        addFunction.accept("Number.xor", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asNumber().orElseThrow(() -> OahuError.failure("NUMBER_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            var that = Util.cast(Double.class, args.getFirst()).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.getFirst(), "Number.xor@that"));

            return Math.pow(thiz, that);
        }));

        addFunction.accept("Number.and", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asNumber().orElseThrow(() -> OahuError.failure("NUMBER_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            var that = Util.cast(String.class, args.getFirst()).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.getFirst(), "Number.and@that"));

            return Console.truncate(thiz) + that;
        }));

        addFunction.accept("Number.neg", new Stmt.Fun.Link((_, data) -> -data.instance().asNumber().orElseThrow(() -> OahuError.failure("NUMBER_PRIMITIVE_MISMATCH"))));

        addFunction.accept("Number.cmp", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asNumber().orElseThrow(() -> OahuError.failure("NUMBER_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            var that = Util.cast(Double.class, args.getFirst()).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.getFirst(), "Number.cmp@that"));

            return (double) thiz.compareTo(that);
        }));

        addFunction.accept("Number.equ", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asNumber().orElseThrow(() -> OahuError.failure("NUMBER_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            var that = Util.cast(Double.class, args.getFirst()).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.getFirst(), "Number.equ@that"));

            return thiz.equals(that);
        }));

        addFunction.accept("Number.toBoolean", new Stmt.Fun.Link((_, data) -> data.instance().asNumber().orElseThrow(() -> OahuError.failure("NUMBER_PRIMITIVE_MISMATCH")) != 0.0));

        addFunction.accept("Number.toString", new Stmt.Fun.Link((_, data) -> Console.truncate(data.instance().asNumber().orElseThrow(() -> OahuError.failure("NUMBER_PRIMITIVE_MISMATCH")))));

        addFunction.accept("Number.fromUnicode", new Stmt.Fun.Link((_, data) -> String.valueOf((char) data.instance().asNumber().orElseThrow(() -> OahuError.failure("NUMBER_PRIMITIVE_MISMATCH")).intValue())));

        addFunction.accept("Number.isFinite", new Stmt.Fun.Link((_, data) -> Double.isFinite(data.instance().asNumber().orElseThrow(() -> OahuError.failure("NUMBER_PRIMITIVE_MISMATCH")))));

        addFunction.accept("Number.isInfinite", new Stmt.Fun.Link((_, data) -> Double.isInfinite(data.instance().asNumber().orElseThrow(() -> OahuError.failure("NUMBER_PRIMITIVE_MISMATCH")))));

        addFunction.accept("Number.isNaN", new Stmt.Fun.Link((_, data) -> Double.isNaN(data.instance().asNumber().orElseThrow(() -> OahuError.failure("NUMBER_PRIMITIVE_MISMATCH")))));
    }

    private void linkString(BiConsumer<String, Stmt.Fun.Link> addFunction) {
        addFunction.accept("String.add", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asString().orElseThrow(() -> OahuError.failure("STRING_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            var that = Util.cast(String.class, args.getFirst()).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.getFirst(), "String.add@that"));

            return thiz + that;
        }));

        addFunction.accept("String.mul", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asString().orElseThrow(() -> OahuError.failure("STRING_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            var that = Util.cast(Double.class, args.getFirst()).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.getFirst(), "String.mul@that"));

            return thiz.repeat(that.intValue());
        }));

        addFunction.accept("String.and", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asString().orElseThrow(() -> OahuError.failure("STRING_PRIMITIVE_MISMATCH"));

            var that = data.args().getFirst();

            return thiz + that;
        }));

        addFunction.accept("String.cmp", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asString().orElseThrow(() -> OahuError.failure("STRING_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            var that = Util.cast(String.class, args.getFirst()).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.getFirst(), "String.cmp@that"));

            return (double) thiz.compareTo(that);
        }));

        addFunction.accept("String.equ", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asString().orElseThrow(() -> OahuError.failure("STRING_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            var that = Util.cast(String.class, args.getFirst()).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.getFirst(), "String.equ@that"));

            return thiz.equals(that);
        }));

        addFunction.accept("String.get", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asString().orElseThrow(() -> OahuError.failure("STRING_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            var index = Util.cast(Double.class, args.getFirst()).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.getFirst(), "String.get@index"));

            if (index < thiz.length()) {
                return thiz.charAt(index.intValue()) + "";
            }

            throw OahuError.invalidStringIndexError(index);
        }));

        addFunction.accept("String.in", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asString().orElseThrow(() -> OahuError.failure("STRING_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            var that = Util.cast(String.class, args.getFirst()).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.getFirst(), "String.in@that"));

            return thiz.contains(that);
        }));

        addFunction.accept("String.toBoolean", new Stmt.Fun.Link((_, data) -> {
            var thiz = data.instance().asString().orElseThrow(() -> OahuError.failure("STRING_PRIMITIVE_MISMATCH"));

            return Boolean.parseBoolean(thiz);
        }));

        addFunction.accept("String.toNumber", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asString().orElseThrow(() -> OahuError.failure("STRING_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            var radix = Util.cast(Double.class, args.getFirst()).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.getFirst(), "String.toNumber@radix"));

            if (radix != 10) {
                return (double) Integer.parseInt(thiz, radix.intValue());
            }
            else {
                try {
                    return Double.parseDouble(thiz);
                }
                catch (NumberFormatException e) {
                    return Double.NaN;
                }
            }
        }));

        addFunction.accept("String.toString", new Stmt.Fun.Link((_, data) -> data.instance().asString().orElseThrow(() -> OahuError.failure("STRING_PRIMITIVE_MISMATCH"))));

        addFunction.accept("String.toUnicode", new Stmt.Fun.Link((_, data) -> (double) data.instance().asString().orElseThrow(() -> OahuError.failure("STRING_PRIMITIVE_MISMATCH")).charAt(0)));

        addFunction.accept("String.size", new Stmt.Fun.Link((_, data) -> (double) data.instance().asString().orElseThrow(() -> OahuError.failure("STRING_PRIMITIVE_MISMATCH")).length()));

        addFunction.accept("String.find", new Stmt.Fun.Link(2, (_, data) -> {
            var thiz = data.instance().asString().orElseThrow(() -> OahuError.failure("STRING_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            var substring = Util.cast(String.class, args.get(0)).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.getFirst(), "String.find@substring"));
            var from = Util.cast(Double.class, args.get(1)).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.get(1), "String.find@from"));

            return thiz.indexOf(substring, from.intValue());
        }));

        addFunction.accept("String.findLast", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asString().orElseThrow(() -> OahuError.failure("STRING_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            var substring = Util.cast(String.class, args.getFirst()).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.getFirst(), "String.findLast@substring"));

            return thiz.lastIndexOf(substring);
        }));

        addFunction.accept("String.startsWith", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asString().orElseThrow(() -> OahuError.failure("STRING_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            var substring = Util.cast(String.class, args.getFirst()).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.getFirst(), "String.startsWith@substring"));

            return thiz.startsWith(substring);
        }));

        addFunction.accept("String.endsWith", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asString().orElseThrow(() -> OahuError.failure("STRING_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            var substring = Util.cast(String.class, args.getFirst()).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.getFirst(), "String.endsWith@substring"));

            return thiz.endsWith(substring);
        }));

        addFunction.accept("String.split", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asString().orElseThrow(() -> OahuError.failure("STRING_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            var regex = Util.cast(String.class, args.getFirst()).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.getFirst(), "String.split@regex"));

            return new Table(Collections.singletonList(Arrays.stream(thiz.split(regex)).toList()), Map.of());
        }));

        addFunction.accept("String.pad", new Stmt.Fun.Link(3, (_, data) -> {
            var thiz = data.instance().asString().orElseThrow(() -> OahuError.failure("STRING_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            var left = Util.cast(Double.class, args.get(0)).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.getFirst(), "String.pad@left"));
            var right = Util.cast(Double.class, args.get(1)).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.get(1), "String.pad@right"));
            var string = Util.cast(String.class, args.get(2)).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.get(2), "String.pad@string"));

            return string.repeat(left.intValue()) + thiz + string.repeat(right.intValue());
        }));

        addFunction.accept("String.toLower", new Stmt.Fun.Link((_, data) -> data.instance().asString().orElseThrow(() -> OahuError.failure("STRING_PRIMITIVE_MISMATCH")).toLowerCase()));

        addFunction.accept("String.toUpper", new Stmt.Fun.Link((_, data) -> data.instance().asString().orElseThrow(() -> OahuError.failure("STRING_PRIMITIVE_MISMATCH")).toUpperCase()));

        addFunction.accept("String.trim", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asString().orElseThrow(() -> OahuError.failure("STRING_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            var indent = Util.cast(Boolean.class, args.getFirst()).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.getFirst(), "String.pad@left"));

            return ((indent) ? thiz.stripIndent() : thiz).trim();
        }));

        addFunction.accept("String.isAlpha", new Stmt.Fun.Link((_, data) -> data.instance().asString().orElseThrow(() -> OahuError.failure("STRING_PRIMITIVE_MISMATCH")).chars().allMatch(Character::isLetter)));

        addFunction.accept("String.isDigit", new Stmt.Fun.Link((_, data) -> data.instance().asString().orElseThrow(() -> OahuError.failure("STRING_PRIMITIVE_MISMATCH")).chars().allMatch(Character::isDigit)));

        addFunction.accept("String.isAlnum", new Stmt.Fun.Link((_, data) -> data.instance().asString().orElseThrow(() -> OahuError.failure("STRING_PRIMITIVE_MISMATCH")).chars().allMatch(Character::isLetterOrDigit)));

        addFunction.accept("String.isLower", new Stmt.Fun.Link((_, data) -> data.instance().asString().orElseThrow(() -> OahuError.failure("STRING_PRIMITIVE_MISMATCH")).chars().allMatch(Character::isLowerCase)));

        addFunction.accept("String.isUpper", new Stmt.Fun.Link((_, data) -> data.instance().asString().orElseThrow(() -> OahuError.failure("STRING_PRIMITIVE_MISMATCH")).chars().allMatch(Character::isUpperCase)));

        addFunction.accept("String.isSpace", new Stmt.Fun.Link((_, data) -> data.instance().asString().orElseThrow(() -> OahuError.failure("STRING_PRIMITIVE_MISMATCH")).chars().allMatch(Character::isWhitespace)));

        addFunction.accept("String.match", new Stmt.Fun.Link(1, ((_, _) -> 0
            /*var string = data.instance.asString()!!

            var (regex) = data.unwrap()

            regex as? String ?: invalidLinkArgumentError(regex, "String.match@regex")

            var matcher = Pattern.compile(regex).matcher(string)

            if (matcher.matches()) {
                IonMap {
                    put("start", script.toInstance(matcher.start().toDouble()))
                    put("end", script.toInstance(matcher.end().toDouble()))
                    put("groups", script.toInstance(IonList {
                        do {
                            for (i in 0 until matcher.groupCount()) {
                                push(script.toInstance(IonMap {
                                    put("start", script.toInstance(matcher.start(i + 1).toDouble()))
                                    put("end", script.toInstance(matcher.end(i + 1).toDouble()))
                                    put("group", script.toInstance(matcher.group(i + 1)))
                                }));)
                            }
                        }
                        while (matcher.find())
                    }));)
                }
            }
            else {
                Null
            }*/
        )));

        addFunction.accept("String.format", new Stmt.Fun.Link(1, (_, _) -> ""
            /*var string = data.instance.asString()!!

            var (args) = data.args

            args as? Instance ?: invalidLinkArgumentError(args, "String.format@args")

            string.format(*args.asTable()?.toTypedArray())*/
        ));
    }

    private void linkList(BiConsumer<String, Stmt.Fun.Link> addFunction) {
        addFunction.accept("List.add", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asTable().orElseThrow(() -> OahuError.failure("LIST_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            var that = args.getFirst();

            var newList = Table.empty();

            newList.addAll(thiz.list());

            newList.add(that);

            return newList;
        }));

        addFunction.accept("List.mul", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asTable().orElseThrow(() -> OahuError.failure("LIST_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            var that = Util.cast(Double.class, args.getFirst()).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.getFirst(), "List.mul@that"));

            return thiz.repeat(that.intValue());
        }));

        addFunction.accept("List.cmp", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asTable().orElseThrow(() -> OahuError.failure("LIST_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            var that = Util.cast(Double.class, args.getFirst()).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.getFirst(), "List.cmp@that"));

            return 0.0;
        }));

        addFunction.accept("List.get", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asTable().orElseThrow(() -> OahuError.failure("LIST_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            var index = Util.cast(Double.class, args.getFirst()).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.getFirst(), "List.get@index"));

            if (index < 0) {
                throw OahuError.negativeIndexError();
            }

            return thiz.get(index.intValue());
        }));

        addFunction.accept("List.set", new Stmt.Fun.Link(2, (_, data) -> {
            var thiz = data.instance().asTable().orElseThrow(() -> OahuError.failure("LIST_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            var index = Util.cast(Double.class, args.get(0)).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.getFirst(), "List.set@index"));
            var x = Util.cast(Double.class, args.get(1)).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.get(1), "List.set@x"));

            if (index < 0) {
                throw OahuError.negativeIndexError();
            }

            thiz.set(index.intValue(), x);

            return Unit.get();
        }));

        addFunction.accept("List.in", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asTable().orElseThrow(() -> OahuError.failure("LIST_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            var that = Util.cast(Double.class, args.getFirst()).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.getFirst(), "Number.equ@that"));

            return thiz.contains(that);
        }));

        addFunction.accept("List.equ", new Stmt.Fun.Link(1, (_, _) -> {
//            var thiz = data.instance().asTable().orElseThrow(() -> OahuError.failure("LIST_PRIMITIVE_MISMATCH"));
//
//            var(that) = data.args
//
//            when(var x = that.fromInstance()) {
//                is OahuList->{
//                    if (list.size != x.size) {
//                        return @create false
//                    }
//
//                    for ((a, b) in list.zip(x)){
//                        if (a is Instance&&b is Instance && !(a eqv b)){
//                            return @create false
//                        }
//                    }
//
//                    return @create true
//                }
//
//    else->return @create false
//            }
            return false;
        }));

        addFunction.accept("List.size", new Stmt.Fun.Link((_, data) -> (double) data.instance().asTable().orElseThrow(() -> OahuError.failure("LIST_PRIMITIVE_MISMATCH")).size()));

        addFunction.accept("List.find", new Stmt.Fun.Link(1, (_, _) -> {
//            var thiz = data.instance().asTable().orElseThrow(() -> OahuError.failure("LIST_PRIMITIVE_MISMATCH"));
//
//            var(x) = data.args
//
            return Unit.get();//list.find(x)
        }));

        addFunction.accept("List.add", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asTable().orElseThrow(() -> OahuError.failure("LIST_PRIMITIVE_MISMATCH"));

            var x = data.args().getFirst();

            return thiz.add(x);
        }));

        addFunction.accept("List.addAt", new Stmt.Fun.Link(2, (_, data) -> {
            var thiz = data.instance().asTable().orElseThrow(() -> OahuError.failure("LIST_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            var index = Util.cast(Double.class, args.getFirst()).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.getFirst(), "List.addAt@index"));
            var x = data.args().get(1);

            thiz.add(index.intValue(), x);

            return Unit.get();
        }));

        addFunction.accept("List.remove", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asTable().orElseThrow(() -> OahuError.failure("LIST_PRIMITIVE_MISMATCH"));

            var x = data.args().getFirst();

            thiz.remove(x);

            return Unit.get();
        }));

        addFunction.accept("List.removeAt", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asTable().orElseThrow(() -> OahuError.failure("LIST_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            var index = Util.cast(Double.class, data.unwrap().getFirst()).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.getFirst(), "List.addAt@index"));

            return thiz.remove(index.intValue());
        }));

        addFunction.accept("List.push", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asTable().orElseThrow(() -> OahuError.failure("LIST_PRIMITIVE_MISMATCH"));

            var x = data.args().getFirst();

            thiz.push(x);

            return Unit.get();
        }));

        addFunction.accept("List.pop", new Stmt.Fun.Link((_, data) -> data.instance().asTable().orElseThrow(() -> OahuError.failure("LIST_PRIMITIVE_MISMATCH")).pop()));

        addFunction.accept("List.unshift", new Stmt.Fun.Link(1, (_, data) -> {
            var thiz = data.instance().asTable().orElseThrow(() -> OahuError.failure("LIST_PRIMITIVE_MISMATCH"));

            var x = data.args().getFirst();

            thiz.unshift(x);

            return Unit.get();
        }));

        addFunction.accept("List.shift", new Stmt.Fun.Link((_, data) -> data.instance().asTable().orElseThrow(() -> OahuError.failure("LIST_PRIMITIVE_MISMATCH")).shift()));

        addFunction.accept("List.splice", new Stmt.Fun.Link(3, (_, data) -> {
            var thiz = data.instance().asTable().orElseThrow(() -> OahuError.failure("LIST_PRIMITIVE_MISMATCH"));

            var args = data.unwrap();

            var start = Util.cast(Double.class, data.unwrap().get(0)).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.getFirst(), "List.addAt@start"));
            var count = Util.cast(Double.class, data.unwrap().get(1)).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.get(1), "List.addAt@count"));
            var insert = Util.cast(Table.class, data.unwrap().get(2)).orElseThrow(() -> OahuError.invalidLinkArgumentError(args.get(2), "List.addAt@insert"));

            return 0.0;//list.splice(start.toInt(), count.toInt(), insert)
        }));

        addFunction.accept("List.reverse", new Stmt.Fun.Link((_, data) -> {
            var thiz = data.instance().asTable().orElseThrow(() -> OahuError.failure("LIST_PRIMITIVE_MISMATCH"));

            //thiz.reverse();

            return Unit.get();
        }));

        addFunction.accept("List.shuffle", new Stmt.Fun.Link((_, data) -> {
            var thiz = data.instance().asTable().orElseThrow(() -> OahuError.failure("LIST_PRIMITIVE_MISMATCH"));

            //thiz.shuffle()

            return Unit.get();
        }));

        addFunction.accept("List.sort", new Stmt.Fun.Link((_, data) -> {
            var thiz = data.instance().asTable().orElseThrow(() -> OahuError.failure("LIST_PRIMITIVE_MISMATCH"));

            //thiz.stream().sorted().toList();

            return Unit.get();
        }));

        addFunction.accept("List.random", new Stmt.Fun.Link((_, data) -> data.instance().asTable().orElseThrow(() -> OahuError.failure("LIST_PRIMITIVE_MISMATCH")).random()));

        addFunction.accept("List.toString", new Stmt.Fun.Link((_, data) -> data.instance().asTable().orElseThrow(() -> OahuError.failure("LIST_PRIMITIVE_MISMATCH")).toString()));
    }

    private void linkStringBuilder(BiConsumer<String, Stmt.Fun.Link> addFunction) {
        addFunction.accept("StringBuilder.append", new Stmt.Fun.Link(1, (_, data) -> {
            var link = data.unlink(StringBuilder.class).orElseThrow(() -> OahuError.missingClassLinkError("StringBuilder"));

            var x = data.args().getFirst();

            link.append(x.toString());

            return Unit.get();
        }));

        addFunction.accept("StringBuilder.toString", new Stmt.Fun.Link((_, data) -> data.unlink(StringBuilder.class).orElseThrow(() -> OahuError.missingClassLinkError("StringBuilder")).toString()));
    }

    @Override
    public void addClasses(BiConsumer<String, Stmt.Class.Link> addClass) {
        addClass.accept("StringBuilder", new Stmt.Class.Link((_, _) -> new StringBuilder()));
    }

    @Override
    public void close() {
        input.close();
    }
}
