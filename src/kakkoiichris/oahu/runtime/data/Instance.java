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
package kakkoiichris.oahu.runtime.data;

import kakkoiichris.oahu.parser.Stmt;
import kakkoiichris.oahu.runtime.Memory;
import kakkoiichris.oahu.runtime.Runtime;

import java.util.Optional;

public class Instance extends Memory.Scope {
    private final Stmt.Class clazz;
    private final Runtime runtime;

    private Object link;

    public Instance(Stmt.Class clazz, Memory.Scope parent, Runtime runtime) {
        super(Optional.ofNullable(clazz).map(Stmt.Class::path).orElse(""), parent);

        this.clazz = clazz;
        this.runtime = runtime;
    }

    public Stmt.Class getClazz() {
        return clazz;
    }

    public Object getLink() {
        return link;
    }

    public Optional<Instance> getBase() {
        if (parent instanceof Instance instance) {
            return Optional.of(instance);
        }

        return Optional.empty();
    }

    public Optional<Boolean> asBoolean() {
        if (this instanceof Primitive.Bool bool) {
            return Optional.of(bool.primitive);
        }

        return Optional.empty();
    }

    public Optional<Double> asNumber() {
        if (this instanceof Primitive.Number number) {
            return Optional.of(number.primitive);
        }

        return Optional.empty();
    }

    public Optional<String> asString() {
        if (this instanceof Primitive.String string) {
            return Optional.of(string.primitive);
        }

        return Optional.empty();
    }

    public Optional<Table> asTable() {
        if (this instanceof Primitive.Table table) {
            return Optional.of(table.primitive);
        }

        return Optional.empty();
    }

    public static Object fromInstance(Object x) {
        if (x instanceof Primitive<?> p) {
            return p.getPrimitive();
        }

        return x;
    }

    public static abstract sealed class Primitive<T> extends Instance {
        public Primitive(Stmt.Class clazz, Memory.Scope parent, Runtime runtime) {
            super(clazz, parent, runtime);
        }

        public abstract T getPrimitive();

        public static final class Bool extends Primitive<Boolean> {
            private final boolean primitive;

            public Bool(Stmt.Class clazz, Memory.Scope parent, Runtime runtime, boolean primitive) {
                super(clazz, parent, runtime);

                this.primitive = primitive;
            }

            public Boolean getPrimitive() {
                return primitive;
            }
        }

        public static final class Number extends Primitive<Double> {
            private final double primitive;

            public Number(Stmt.Class clazz, Memory.Scope parent, Runtime runtime, double primitive) {
                super(clazz, parent, runtime);

                this.primitive = primitive;
            }

            public Double getPrimitive() {
                return primitive;
            }
        }

        public static final class String extends Primitive<java.lang.String> {
            private final java.lang.String primitive;

            public String(Stmt.Class clazz, Memory.Scope parent, Runtime runtime, java.lang.String primitive) {
                super(clazz, parent, runtime);

                this.primitive = primitive;
            }

            public java.lang.String getPrimitive() {
                return primitive;
            }
        }

        public static final class Table extends Primitive<kakkoiichris.oahu.runtime.data.Table> {
            private final kakkoiichris.oahu.runtime.data.Table primitive;

            public Table(Stmt.Class clazz, Memory.Scope parent, Runtime runtime, kakkoiichris.oahu.runtime.data.Table primitive) {
                super(clazz, parent, runtime);

                this.primitive = primitive;
            }

            public kakkoiichris.oahu.runtime.data.Table getPrimitive() {
                return primitive;
            }
        }
    }
}
