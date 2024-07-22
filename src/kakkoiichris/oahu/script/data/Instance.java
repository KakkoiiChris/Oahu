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
package kakkoiichris.oahu.script.data;

import kakkoiichris.oahu.parser.Stmt;
import kakkoiichris.oahu.script.Memory;
import kakkoiichris.oahu.script.Script;

import java.util.Optional;

public class Instance extends Memory.Scope {
    private final Stmt.Class clazz;
    private final Script script;

    private Object link;

    public Instance(Stmt.Class clazz, Memory.Scope parent, Script script) {
        super(Optional.ofNullable(clazz).map(Stmt.Class::path).orElse(""), parent);

        this.clazz = clazz;
        this.script = script;
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

    public static Object fromInstance(Object x) {
        if (x instanceof Primitive<?> p) {
            return p.getPrimitive();
        }

        return x;
    }

    public static abstract sealed class Primitive<T> extends Instance {
        public Primitive(Stmt.Class clazz, Memory.Scope parent, Script script) {
            super(clazz, parent, script);
        }

        public abstract T getPrimitive();

        public static final class Bool extends Primitive<Boolean> {
            private final boolean primitive;

            public Bool(Stmt.Class clazz, Memory.Scope parent, Script script, boolean primitive) {
                super(clazz, parent, script);
                this.primitive = primitive;
            }

            public Boolean getPrimitive() {
                return primitive;
            }
        }

        public static final class Number extends Primitive<Double> {
            private final double primitive;

            public Number(Stmt.Class clazz, Memory.Scope parent, Script script, double primitive) {
                super(clazz, parent, script);
                this.primitive = primitive;
            }

            public Double getPrimitive() {
                return primitive;
            }
        }

        public static final class String extends Primitive<java.lang.String> {
            private final java.lang.String primitive;

            public String(Stmt.Class clazz, Memory.Scope parent, Script script, java.lang.String primitive) {
                super(clazz, parent, script);
                this.primitive = primitive;
            }

            public java.lang.String getPrimitive() {
                return primitive;
            }
        }

        public static final class Table extends Primitive<kakkoiichris.oahu.script.data.Table> {
            private final kakkoiichris.oahu.script.data.Table primitive;

            public Table(Stmt.Class clazz, Memory.Scope parent, Script script, kakkoiichris.oahu.script.data.Table primitive) {
                super(clazz, parent, script);
                this.primitive = primitive;
            }

            public kakkoiichris.oahu.script.data.Table getPrimitive() {
                return primitive;
            }
        }
    }
}
