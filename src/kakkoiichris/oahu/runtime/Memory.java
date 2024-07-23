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
package kakkoiichris.oahu.runtime;

import kakkoiichris.oahu.runtime.data.Instance;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

public class Memory {
    private final Stack<Scope> scopes = new Stack<>();

    private final Map<Object, Instance.Primitive<?>> primitives = new HashMap<>();

    public void push(Scope scope) {
        scopes.push(scope);
    }

    public void push() {
        scopes.push(new Scope("", peek()));
    }

    public void pushGlobal() {
        scopes.push(new Scope("", null));
    }

    public void pop() {
        scopes.pop();
    }

    public Scope peek() {
        return scopes.peek();
    }

    public boolean isEmpty() {
        return scopes.isEmpty();
    }

    public Optional<Reference> get(String name) {
        for (var scope : scopes) {
            var ref = scope.get(name);

            if (ref.isPresent()) {
                return ref;
            }
        }

        return Optional.empty();
    }

    public boolean newRef(boolean constant, boolean mutable, String name, Object value) {
        return scopes.peek().newRef(constant, mutable, name, value);
    }

    public boolean newLet(boolean mutable,String name, Object value) {
        return scopes.peek().newLet(mutable, name, value);
    }

    public boolean newVar(boolean mutable,String name, Object value) {
        return scopes.peek().newVar(mutable, name, value);
    }

    public Optional<Instance.Primitive<?>> getPrimitive(Object key) {
        return Optional.ofNullable(primitives.get(key));
    }

    public void setPrimitive(Object key, Instance.Primitive<?> primitive) {
        primitives.put(key, primitive);
    }

    public static class Scope {
        protected final String id;
        protected final Scope parent;

        protected final Map<String, Reference> references = new HashMap<>();

        public Scope(String id, Scope parent) {
            this.id = id;
            this.parent = parent;
        }

        public Optional<Reference> get(String name) {
            if (references.containsKey(name)) {
                return Optional.of(references.get(name));
            }

            return Optional.empty();
        }

        public boolean newRef(boolean constant, boolean mutable, String name, Object value) {
            var reference = get(name);

            if (reference.isPresent()) {
                return false;
            }

            references.put(name, new Reference(constant, mutable, value));

            return true;
        }

        public boolean newLet(boolean mutable, String name, Object value) {
            return newRef(true, mutable, name, value);
        }

        public boolean newVar(boolean mutable, String name, Object value) {
            return newRef(false, mutable, name, value);
        }
    }

    public static final class Reference {
        private final boolean isConstant;
        private final boolean isMutable;

        private Object value;

        public Reference(boolean isConstant, boolean isMutable, Object value) {
            this.isConstant = isConstant;
            this.isMutable = isMutable;
            this.value = value;
        }

        public boolean isConstant() {
            return isConstant;
        }

        public boolean isMutable() {
            return isMutable;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }

    public static Object fromReference(Object x) {
        if (x instanceof Reference r) {
            return r.getValue();
        }

        return x;
    }
}
