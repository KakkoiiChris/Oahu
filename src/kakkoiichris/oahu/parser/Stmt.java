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
import kakkoiichris.oahu.script.Memory;
import kakkoiichris.oahu.script.Script;
import kakkoiichris.oahu.script.data.Instance;
import kakkoiichris.oahu.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.BiFunction;

public sealed interface Stmt {
    Context context();

    <X> X accept(Visitor<X> visitor);

    interface Visitor<X> {
        default X visit(Stmt stmt) {
            return stmt.accept(this);
        }

        X visitEmptyStmt(Empty stmt);

        X visitExpressionStmt(Expression stmt);

        X visitDeclarationStmt(Declaration stmt);

        X visitBlockStmt(Block stmt);

        X visitWhileStmt(While stmt);

        X visitDoStmt(Do stmt);

        X visitLoopStmt(Loop stmt);

        X visitForStmt(For stmt);

        X visitBreakStmt(Break stmt);

        X visitContinueStmt(Continue stmt);

        X visitReturnStmt(Return stmt);

        X visitThrowStmt(Throw stmt);

        X visitExitStmt(Exit stmt);

        X visitFunStmt(Fun stmt);

        X visitClassStmt(Class stmt);

        X visitEnumStmt(Enum stmt);
    }

    final class Empty implements Stmt {
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
            return visitor.visitEmptyStmt(this);
        }

        @Override
        public String toString() {
            return "empty";
        }
    }

    record Expression(Context context, Expr expr) implements Stmt {
        @Override
        public <X> X accept(Visitor<X> visitor) {
            return visitor.visitExpressionStmt(this);
        }

        @Override
        public String toString() {
            return "expr";
        }
    }

    record Declaration(
        Context context,
        boolean constant,
        boolean mutable,
        boolean destructured,
        List<Expr.Name> names,
        Expr expr
    ) implements Stmt {
        @Override
        public <X> X accept(Visitor<X> visitor) {
            return visitor.visitDeclarationStmt(this);
        }

        @Override
        public String toString() {
            var keyword = constant ? "let" : "var";

            if (destructured) {
                var joiner = new StringJoiner(", ");

                for (var name : names) {
                    joiner.add(name.toString());
                }

                return "%s (%s)".formatted(keyword, joiner.toString());
            }

            return "%s %s".formatted(keyword, names.get(0).toString());
        }
    }

    record Block(Context context, List<Stmt> stmts) implements Stmt {
        @Override
        public <X> X accept(Visitor<X> visitor) {
            return visitor.visitBlockStmt(this);
        }

        @Override
        public String toString() {
            return "{ ... }";
        }
    }

    record While(Context context, Expr.Name label, Expr condition, Stmt body) implements Stmt {
        @Override
        public <X> X accept(Visitor<X> visitor) {
            return visitor.visitWhileStmt(this);
        }

        @Override
        public String toString() {
            if (label.isEmpty()) {
                return "while";
            }

            return "while @ %s".formatted(label);
        }
    }

    record Do(Context context, Expr.Name label, Stmt body, Expr condition) implements Stmt {
        @Override
        public <X> X accept(Visitor<X> visitor) {
            return visitor.visitDoStmt(this);
        }

        @Override
        public String toString() {
            if (label.isEmpty()) {
                return "do";
            }

            return "do @ %s".formatted(label);
        }
    }

    record Loop(Context context, Expr.Name label, Expr count, Stmt body) implements Stmt {
        @Override
        public <X> X accept(Visitor<X> visitor) {
            return visitor.visitLoopStmt(this);
        }

        @Override
        public String toString() {
            if (label.isEmpty()) {
                return "loop";
            }

            return "loop @ %s".formatted(label);
        }
    }

    record For(Context context, Expr.Name label, boolean destructured, List<Expr.Name> names, Expr iterable,
               Stmt body) implements Stmt {
        @Override
        public <X> X accept(Visitor<X> visitor) {
            return visitor.visitForStmt(this);
        }

        @Override
        public String toString() {
            if (label.isEmpty()) {
                if (destructured) {
                    var joiner = new StringJoiner(", ");

                    for (var name : names) {
                        joiner.add(name.toString());
                    }

                    return "for (%s)".formatted(joiner.toString());
                }

                return "for %s".formatted(names.get(0).toString());
            }

            if (destructured) {
                var joiner = new StringJoiner(", ");

                for (var name : names) {
                    joiner.add(name.toString());
                }

                return "for @ %s (%s)".formatted(label, joiner.toString());
            }

            return "for @ %s %s".formatted(label, names.get(0).toString());
        }
    }

    record Break(Context context, Expr.Name label) implements Stmt {
        @Override
        public <X> X accept(Visitor<X> visitor) {
            return visitor.visitBreakStmt(this);
        }

        @Override
        public String toString() {
            if (label.isEmpty()) {
                return "break";
            }

            return "break @ %s".formatted(label);
        }
    }

    record Continue(Context context, Expr.Name label) implements Stmt {
        @Override
        public <X> X accept(Visitor<X> visitor) {
            return visitor.visitContinueStmt(this);
        }

        @Override
        public String toString() {
            if (label.isEmpty()) {
                return "continue";
            }

            return "continue @ %s".formatted(label);
        }
    }

    record Throw(Context context, Expr expr) implements Stmt {
        @Override
        public <X> X accept(Visitor<X> visitor) {
            return visitor.visitThrowStmt(this);
        }

        @Override
        public String toString() {
            return "throw";
        }
    }

    record Return(Context context, Expr expr) implements Stmt {
        @Override
        public <X> X accept(Visitor<X> visitor) {
            return visitor.visitReturnStmt(this);
        }

        @Override
        public String toString() {
            return "return";
        }
    }

    record Exit(Context context, Expr expr) implements Stmt {
        @Override
        public <X> X accept(Visitor<X> visitor) {
            return visitor.visitExitStmt(this);
        }

        @Override
        public String toString() {
            return "exit";
        }
    }

    final class Fun implements Stmt, Callable<Fun.Param> {
        private final Context context;
        private final String path;
        private final boolean isLinked;
        private final Expr.Name name;
        private final List<Param> params;
        private final Stmt body;

        private Memory.Scope scope;

        private Link link;

        public Fun(Context context, String path, boolean isLinked, Expr.Name name, List<Param> params, Stmt body) {
            this.context = context;
            this.path = path;
            this.isLinked = isLinked;
            this.name = name;
            this.params = params;
            this.body = body;
        }

        @Override
        public Context context() {
            return context;
        }

        public String path() {
            return path;
        }

        public boolean isLinked() {
            return isLinked;
        }

        public Expr.Name name() {
            return name;
        }

        @Override
        public List<Param> params() {
            return params;
        }

        public Stmt body() {
            return body;
        }

        public Memory.Scope scope() {
            return scope;
        }

        public void setScope(Memory.Scope scope) {
            this.scope = scope;
        }

        public Link getLink() {
            return link;
        }

        public void setLink(Link link) {
            this.link = link;
        }

        public Fun copy() {
            var copy = new Fun(context(), path, isLinked, name, params, body);

            if (isLinked) {
                copy.setLink(link);
            }

            return copy;
        }

        public Fun copyWithExtension() {
            return new Fun(context(), "", isLinked, name, params, body);
        }

        @Override
        public int arity() {
            return params.size();
        }

        @Override
        public <X> X accept(Visitor<X> visitor) {
            return visitor.visitFunStmt(this);
        }

        @Override
        public String toString() {
            return "fun %s".formatted(name);
        }

        public record Param(
            Context context,
            boolean isMutable,
            boolean isVarargs,
            Expr.Name name,
            Expr defaultValue
        ) implements Callable.Param {
            @Override
            public String toString() {
                var string = "";

                if (isVarargs) {
                    string += '*';
                }

                string += name;

                if (defaultValue == Expr.Empty.get()) {
                    string += " = ...";
                }

                return string;
            }
        }

        public record Link(int arity, BiFunction<Script, Data, Object> method) {
            public Link(BiFunction<Script, Data, Object> method) {
                this(0, method);
            }

            public boolean resolve(List<Object> args) {
                return args.size() == arity;
            }

            public Object invoke(Script script, Instance instance, List<Object> args) {
                return method.apply(script, new Data(instance, args));
            }

            public record Data(Instance instance, List<Object> args) {
                public <X> Optional<X> unlink(java.lang.Class<X> clazz) {
                    return Util.cast(clazz, instance.getLink());
                }

                public List<Object> unwrap() {
                    var list = new ArrayList<>();

                    for (var arg : args) {
                        var value = Instance.fromInstance(arg);

                        list.add(value);
                    }

                    return list;
                }
            }
        }
    }

    final class Class implements Stmt, Callable<Class.Param> {
        private final Context context;
        private final String path;
        private final boolean isLinked;
        private final Expr.Name name;
        private final List<Param> params;
        private final Expr base;
        private final List<Stmt> init;

        private Link link;

        public Class(Context context, String path, boolean isLinked, Expr.Name name, List<Param> params, Expr base, List<Stmt> init) {
            this.context = context;
            this.path = path;
            this.isLinked = isLinked;
            this.name = name;
            this.params = params;
            this.base = base;
            this.init = init;
        }

        @Override
        public Context context() {
            return context;
        }

        public String path() {
            return path;
        }

        public boolean isLinked() {
            return isLinked;
        }

        public Expr.Name name() {
            return name;
        }

        public List<Param> params() {
            return params;
        }

        public Expr base() {
            return base;
        }

        public List<Stmt> init() {
            return init;
        }

        public Link link() {
            return link;
        }

        public void setLink(Link link) {
            this.link = link;
        }

        @Override
        public int arity() {
            return params.size();
        }

        @Override
        public <X> X accept(Visitor<X> visitor) {
            return visitor.visitClassStmt(this);
        }

        @Override
        public String toString() {
            return "class %s".formatted(name);
        }

        public record Param(
            Context context,
            Boolean isConstant,
            boolean isMutable,
            boolean isVarargs,
            Expr.Name name,
            Expr defaultValue
        ) implements Callable.Param {
            @Override
            public String toString() {
                var string = "";

                if (isVarargs) {
                    string += '*';
                }

                string += name;

                if (defaultValue == Expr.Empty.get()) {
                    string += " = ...";
                }

                return string;
            }
        }

        public record Link(BiFunction<Script, Instance, Object> method) {
            public Object invoke(Script script, Instance instance) {
                return method.apply(script, instance);
            }
        }
    }

    record Enum(Context context, Class clazz, List<Entry> entries) implements Stmt {
        @Override
        public <X> X accept(Visitor<X> visitor) {
            return visitor.visitEnumStmt(this);
        }

        @Override
        public String toString() {
            return "enum %s".formatted(clazz.name());
        }

        public record Entry(Context context, Expr.Name name, List<Expr.Invoke.Arg> args, Expr ordinal) {
            @Override
            public String toString() {
                return name.toString();
            }
        }
    }
}