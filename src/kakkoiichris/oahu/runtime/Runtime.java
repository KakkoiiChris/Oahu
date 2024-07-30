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

import kakkoiichris.oahu.parser.Callable;
import kakkoiichris.oahu.parser.Expr;
import kakkoiichris.oahu.parser.Program;
import kakkoiichris.oahu.parser.Stmt;
import kakkoiichris.oahu.runtime.data.Instance;
import kakkoiichris.oahu.runtime.data.Null;
import kakkoiichris.oahu.runtime.data.Table;
import kakkoiichris.oahu.runtime.data.Unit;
import kakkoiichris.oahu.runtime.linker.Link;
import kakkoiichris.oahu.runtime.linker.Linker;
import kakkoiichris.oahu.util.OahuError;
import kakkoiichris.oahu.util.Source;
import kakkoiichris.oahu.util.Util;

import java.util.*;

public class Runtime implements Expr.Visitor<Object>, Stmt.Visitor<Unit> {
    private static final Map<Class<?>, String> primitives = Map.of(
        Boolean.class, "Bool",
        Double.class, "Number",
        String.class, "String",
        Table.class, "Table"
    );

    private final Memory memory = new Memory();

    private final Source source;
    private final Program program;
    private final Linker linker;

    public Runtime(Source source, Program program, Link... links) {
        this.source = source;
        this.program = program;

        linker = new Linker(source, links);
    }

    public Result run() {
        try {
            memory.pushGlobal();

            for (var stmt : program) {
                visit(stmt);
            }
        }
        catch (Redirect.Return redirect) {
            var value = redirect.getValue();

            return new Result(value, value.toString());
        }
        finally {
            memory.pop();
        }

        return new Result(Unit.get(), "");
    }

    @Override
    public Object visitEmptyExpr(Expr.Empty expr) {
        return Unit.get();
    }

    @Override
    public Object visitValueExpr(Expr.Value expr) {
        return expr.value();
    }

    @Override
    public Object visitNameExpr(Expr.Name expr) {
        return memory
            .get(expr.value())
            .orElseThrow(() -> OahuError.undefinedName(expr, source, expr.context()));
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        var e = Memory.fromReference(visit(expr.expr()));

        return switch (expr.operator()) {
            case NEGATIVE -> {
                if (e instanceof Double d) {
                    yield -d;
                }

                if (e instanceof String s) {
                    yield new StringBuilder(s)
                        .reverse()
                        .toString();
                }

                throw OahuError.invalidUnaryOperand(e, expr.operator(), source, expr.context());
            }

            case NOT -> {
                if (e instanceof Boolean b) {
                    yield !b;
                }

                throw OahuError.invalidUnaryOperand(e, expr.operator(), source, expr.context());
            }

            default -> Unit.get();
        };
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        return switch (expr.operator()) {
            case OR -> {
                var l = Memory.fromReference(visit(expr.left()));

                if (l instanceof Boolean b && b) {
                    yield true;
                }

                var r = Memory.fromReference(visit(expr.right()));

                if (r instanceof Boolean) {
                    yield r;
                }

                throw OahuError.invalidLeftOperand(l, expr.operator(), source, expr.left().context());
            }

            case AND -> {
                var l = Memory.fromReference(visit(expr.left()));

                if (l instanceof Boolean b && !b) {
                    yield false;
                }

                var r = Memory.fromReference(visit(expr.right()));

                if (r instanceof Boolean) {
                    yield r;
                }

                throw OahuError.invalidLeftOperand(l, expr.operator(), source, expr.left().context());
            }

            case EQUAL -> {
                var l = Memory.fromReference(visit(expr.left()));
                var r = Memory.fromReference(visit(expr.right()));

                yield l.equals(r);
            }

            case NOT_EQUAL -> {
                var l = Memory.fromReference(visit(expr.left()));
                var r = Memory.fromReference(visit(expr.right()));

                yield !l.equals(r);
            }

            case LESS -> {
                var l = Memory.fromReference(visit(expr.left()));

                if (l instanceof Double da) {
                    var r = Memory.fromReference(visit(expr.right()));

                    if (r instanceof Double db) {
                        yield da < db;
                    }

                    throw OahuError.invalidRightOperand(r, expr.operator(), source, expr.right().context());
                }

                if (l instanceof String sa) {
                    var r = Memory.fromReference(visit(expr.right()));

                    if (r instanceof String sb) {
                        yield sa.compareTo(sb) < 0;
                    }

                    throw OahuError.invalidRightOperand(r, expr.operator(), source, expr.right().context());
                }

                throw OahuError.invalidLeftOperand(l, expr.operator(), source, expr.left().context());
            }

            case LESS_EQUAL -> {
                var l = Memory.fromReference(visit(expr.left()));

                if (l instanceof Double da) {
                    var r = Memory.fromReference(visit(expr.right()));

                    if (r instanceof Double db) {
                        yield da <= db;
                    }

                    throw OahuError.invalidRightOperand(r, expr.operator(), source, expr.right().context());
                }

                if (l instanceof String sa) {
                    var r = Memory.fromReference(visit(expr.right()));

                    if (r instanceof String sb) {
                        yield sa.compareTo(sb) <= 0;
                    }

                    throw OahuError.invalidRightOperand(r, expr.operator(), source, expr.right().context());
                }

                throw OahuError.invalidLeftOperand(l, expr.operator(), source, expr.left().context());
            }

            case GREATER -> {
                var l = Memory.fromReference(visit(expr.left()));

                if (l instanceof Double da) {
                    var r = Memory.fromReference(visit(expr.right()));

                    if (r instanceof Double db) {
                        yield da > db;
                    }

                    throw OahuError.invalidRightOperand(r, expr.operator(), source, expr.right().context());
                }

                if (l instanceof String sa) {
                    var r = Memory.fromReference(visit(expr.right()));

                    if (r instanceof String sb) {
                        yield sa.compareTo(sb) > 0;
                    }

                    throw OahuError.invalidRightOperand(r, expr.operator(), source, expr.right().context());
                }

                throw OahuError.invalidLeftOperand(l, expr.operator(), source, expr.left().context());
            }

            case GREATER_EQUAL -> {
                var l = Memory.fromReference(visit(expr.left()));

                if (l instanceof Double da) {
                    var r = Memory.fromReference(visit(expr.right()));

                    if (r instanceof Double db) {
                        yield da >= db;
                    }

                    throw OahuError.invalidRightOperand(r, expr.operator(), source, expr.right().context());
                }

                if (l instanceof String sa) {
                    var r = Memory.fromReference(visit(expr.right()));

                    if (r instanceof String sb) {
                        yield sa.compareTo(sb) >= 0;
                    }

                    throw OahuError.invalidRightOperand(r, expr.operator(), source, expr.right().context());
                }

                throw OahuError.invalidLeftOperand(l, expr.operator(), source, expr.left().context());
            }

            case ADD -> {
                var l = Memory.fromReference(visit(expr.left()));

                if (l instanceof Double da) {
                    var r = Memory.fromReference(visit(expr.right()));

                    if (r instanceof Double db) {
                        yield da + db;
                    }

                    if (r instanceof String sb) {
                        yield da + sb;
                    }

                    throw OahuError.invalidRightOperand(r, expr.operator(), source, expr.right().context());
                }

                if (l instanceof String sa) {
                    var r = Memory.fromReference(visit(expr.right()));

                    if (r instanceof Double db) {
                        yield sa + db;
                    }

                    if (r instanceof String sb) {
                        yield sa + sb;
                    }

                    throw OahuError.invalidRightOperand(r, expr.operator(), source, expr.right().context());
                }

                throw OahuError.invalidLeftOperand(l, expr.operator(), source, expr.left().context());
            }

            case SUBTRACT -> {
                var l = Memory.fromReference(visit(expr.left()));

                if (l instanceof Double da) {
                    var r = Memory.fromReference(visit(expr.right()));

                    if (r instanceof Double db) {
                        yield da - db;
                    }

                    throw OahuError.invalidRightOperand(r, expr.operator(), source, expr.right().context());
                }

                throw OahuError.invalidLeftOperand(l, expr.operator(), source, expr.left().context());
            }

            case MULTIPLY -> {
                var l = Memory.fromReference(visit(expr.left()));

                if (l instanceof Double da) {
                    var r = Memory.fromReference(visit(expr.right()));

                    if (r instanceof Double db) {
                        yield da * db;
                    }

                    throw OahuError.invalidRightOperand(r, expr.operator(), source, expr.right().context());
                }

                if (l instanceof String sa) {
                    var r = Memory.fromReference(visit(expr.right()));

                    if (r instanceof Double db) {
                        yield sa.repeat(db.intValue());
                    }

                    throw OahuError.invalidRightOperand(r, expr.operator(), source, expr.right().context());
                }

                throw OahuError.invalidLeftOperand(l, expr.operator(), source, expr.left().context());
            }

            case DIVIDE -> {
                var l = Memory.fromReference(visit(expr.left()));

                if (l instanceof Double da) {
                    var r = Memory.fromReference(visit(expr.right()));

                    if (r instanceof Double db) {
                        yield da / db;
                    }

                    throw OahuError.invalidRightOperand(r, expr.operator(), source, expr.right().context());
                }

                throw OahuError.invalidLeftOperand(l, expr.operator(), source, expr.left().context());
            }

            case MODULUS -> {
                var l = Memory.fromReference(visit(expr.left()));

                if (l instanceof Double da) {
                    var r = Memory.fromReference(visit(expr.right()));

                    if (r instanceof Double db) {
                        yield da % db;
                    }

                    throw OahuError.invalidRightOperand(r, expr.operator(), source, expr.right().context());
                }

                throw OahuError.invalidLeftOperand(l, expr.operator(), source, expr.left().context());
            }
        };
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        var value = Memory.fromReference(visit(expr.value()));

        var refOpt = memory.get(expr.name().value());

        if (refOpt.isEmpty()) {
            throw OahuError.undefinedName(expr.name(), source, expr.name().context());
        }

        var ref = refOpt.get();

        if (ref.isConstant()) {
            throw OahuError.reassignedConstant(source, expr.name().context());
        }

        ref.setValue(value);

        return value;
    }

    @Override
    public Object visitIndexExpr(Expr.Index expr) {
        return null;
    }

    @Override
    public Object visitInvokeExpr(Expr.Invoke expr) {
        var target = visit(expr.target());

        Callable<?> callable = Util.cast(Callable.class, target).orElseThrow(() -> OahuError.notCallableValueError(target, source, expr.context()));

        var exprs = callable.resolve(expr.args()).orElseThrow(); // TODO Resolve exprs

        var args = new ArrayList<>();

        for (var arg : exprs) {
            var argValue = visit(arg);

            if (!(argValue instanceof Expr.Invoke.Vararg vararg)) {
                continue;
            }

            var list = new ArrayList<>();

            for (var subExpr : vararg.exprs()) {
                var subValue = visit(subExpr);

                if (!(subValue instanceof Expr.Invoke.Spread spread)) {
                    list.add(subValue);

                    continue;
                }

                var value = visit(spread.expr());

                if (!(value instanceof Instance.Primitive<?> primitive)) {
                    throw OahuError.notSpreadableValueError(value, source, spread.expr().context());
                }

                switch (primitive) {
                    case Instance.Primitive.String string -> {
                        for (var c : string.getPrimitive().toCharArray()) {
                            list.add(toInstance(String.valueOf(c)).orElseThrow());
                        }
                    }

                    case Instance.Primitive.Table table -> {
                        for (var x : table.getPrimitive()) {
                            list.add(x);
                        }
                    }

                    default -> throw OahuError.notSpreadableValueError(primitive, source, spread.expr().context());
                }
            }

            args.add(toInstance(new Table(list, new HashMap<>())).orElseThrow());
        }

        return switch (callable) {
            case Stmt.Fun fun -> invokeFun(fun, args);

            case Stmt.Class clazz -> invokeClass(clazz, args);
        };
    }

    private Object invokeFun(Stmt.Fun fun, List<Object> args) {
        var scope = fun.scope();

        Object result;

        try {
            memory.push(new Memory.Scope(String.format("%s inner", fun.path()), scope));

            if (fun.isLinked()) {
                if (!fun.getLink().resolve(args)) {
                    throw OahuError.linkResolutionError(fun.path(), fun.getLink().arity(), args.size());
                }

                var instance = (scope instanceof Instance i) ? i : Null.get();

                result = fun.getLink().invoke(this, instance, args);
            }
            else {
                for (var i = 0; i < fun.params().size(); i++) {
                    var param = fun.params().get(i);

                    if (!memory.newLet(param.isMutable(), param.name().value(), args.get(i))) {
                        throw OahuError.redefinedName(param.name(), source, param.context());
                    }
                }

                result = visit(fun.body());
            }
        }
        catch (Redirect.Return r) {
            result = r.getValue();
        }
        catch (Redirect.Exit | Redirect.Throw r) {
            throw r;
        }
        catch (Redirect r) {
            throw OahuError.unhandledRedirect(r, source, fun.context());
        }
        finally {
            memory.pop();
        }

        //TODO
        return toInstance(result).orElseThrow();
    }

    private Instance invokeClass(Stmt.Class clazz, List<Object> args, Object primitive) {
        try {
            memory.push();

            for (var i = 0; i < clazz.params().size(); i++) {
                var param = clazz.params().get(i);

                if (!memory.newLet(param.isMutable(), param.name().value(), args.get(i))) {
                    throw OahuError.redefinedName(param.name(), source, param.name().context());
                }
            }

            var base = Util.cast(Memory.Scope.class, Memory.fromReference(visit(clazz.base()))).filter(i -> i != Null.get()).orElse(memory.peek());

            var instance = new Instance(clazz, base, this);

            if (primitives.containsValue(clazz.path())) {
                if (primitive == null) {
                    throw OahuError.failure("Primitive for wrapper class is broken!");
                }

                //instance.setPrimitive(primitive);
            }

            try {
                memory.push(instance);

                for (var param : clazz.params()) {
                    if (param.isConstant() != null) {
                        memory.newRef(param.isConstant(), param.isMutable(), param.name().value(), memory.get(param.name().value()).orElseThrow(() -> OahuError.undefinedName(param.name(), source, param.name().context())));
                    }
                }

                for (var stmt : clazz.init()) {
                    visit(stmt);
                }
            }
            finally {
                memory.pop();
            }

            if (!instance.newLet(false, "this", instance)) {
                throw OahuError.failure("Member 'this' should not exist yet!");
            }

            if (base instanceof Instance) {
                if (!instance.newLet(false, "base", base)) {
                    throw OahuError.failure("Member 'base' should not exist yet!");
                }
            }

            //instance.bubbleUp();

            if (clazz.isLinked()) {
                //instance.setLink(clazz.getLink().invoke(this, instance));
            }
            else {
                //instance.setLink(Util.cast(Instance.class, base).map(Instance::getLink).orElse(null));
            }

            return instance;
        }
        catch (Redirect.Exit r) {
            throw r;
        }
        catch (Redirect r) {
            throw OahuError.unhandledRedirect(r, source, clazz.context());
        }
        finally {
            memory.pop();
        }
    }

    private Instance invokeClass(Stmt.Class clazz, List<Object> args) {
        return invokeClass(clazz, args, null);
    }

    private Instance invokeClass(Stmt.Class clazz, Object primitive) {
        return invokeClass(clazz, List.of(), primitive);
    }

    private Instance invokeClass(Stmt.Class clazz) {
        return invokeClass(clazz, List.of(), null);
    }

    public Optional<Object> toInstance(Object value) {
        if (value instanceof Callable<?> || value instanceof Instance) {
            return Optional.of(value);
        }

        var primitive = memory.getPrimitive(value);

        if (primitive.isPresent()) {
            return Optional.of(primitive.get());
        }

        if (memory.isEmpty()) {
            return Optional.empty();
        }

        var className = primitives.getOrDefault(value.getClass(), "");

        if (className.isEmpty()) {
            return Optional.empty();
        }

        var clazz = memory
            .get(className)
            .flatMap(x -> Util.cast(Stmt.Class.class, x))
            .orElseThrow(() -> OahuError.failure("Broken primitive instance conversion!"));

        //TODO
        var instance = Util.cast(Instance.Primitive.class, invokeClass(clazz, value)).orElseThrow();

        memory.setPrimitive(value, instance);

        return Optional.of(instance);
    }

    @Override
    public Object visitListLiteralExpr(Expr.ListLiteral expr) {
        var list = new ArrayList<>();

        for (var element : expr.elements()) {
            list.add(visit(element));
        }

        var map = new HashMap<String, Object>();

        return new Table(list, map);
    }

    @Override
    public Object visitListForExpr(Expr.ListFor expr) {
        return null;
    }

    @Override
    public Object visitLambdaExpr(Expr.Lambda expr) {
        return expr.fun();
    }

    @Override
    public Object visitBlockExpr(Expr.Block expr) {
        Object last = Unit.get();

        try {
            memory.push();

            for (var subExpr : expr.exprs()) {
                last = visit(subExpr);
            }
        }
        finally {
            memory.pop();
        }

        return last;
    }

    @Override
    public Object visitIfExpr(Expr.If expr) {
        return null;
    }

    @Override
    public Object visitWhenExpr(Expr.When expr) {
        return null;
    }

    @Override
    public Object visitTryExpr(Expr.Try expr) {
        return null;
    }

    @Override
    public Object visitStatementExpr(Expr.Statement expr) {
        return visit(expr.stmt());
    }

    @Override
    public Unit visitEmptyStmt(Stmt.Empty stmt) {
        return Unit.get();
    }

    @Override
    public Unit visitExpressionStmt(Stmt.Expression stmt) {
        visit(stmt.expr());

        return Unit.get();
    }

    @Override
    public Unit visitDeclarationStmt(Stmt.Declaration stmt) {
        var constant = stmt.constant();
        var mutable = stmt.mutable();
        var destructured = stmt.destructured();

        var value = visit(stmt.expr());

        if (!destructured && !memory.newRef(constant, mutable, stmt.names().getFirst().value(), value)) {

        }

        return Unit.get();
    }

    @Override
    public Unit visitBlockStmt(Stmt.Block stmt) {
        return Unit.get();
    }

    @Override
    public Unit visitWhileStmt(Stmt.While stmt) {
        return Unit.get();
    }

    @Override
    public Unit visitDoStmt(Stmt.Do stmt) {
        return Unit.get();
    }

    @Override
    public Unit visitLoopStmt(Stmt.Loop stmt) {
        return Unit.get();
    }

    @Override
    public Unit visitForStmt(Stmt.For stmt) {
        return Unit.get();
    }

    @Override
    public Unit visitBreakStmt(Stmt.Break stmt) {
        throw new Redirect.Break(stmt.label());
    }

    @Override
    public Unit visitContinueStmt(Stmt.Continue stmt) {
        throw new Redirect.Continue(stmt.label());
    }

    @Override
    public Unit visitReturnStmt(Stmt.Return stmt) {
        var value = visit(stmt.expr());

        throw new Redirect.Return(value);
    }

    @Override
    public Unit visitThrowStmt(Stmt.Throw stmt) {
        var value = visit(stmt.expr());

        throw new Redirect.Throw(value);
    }

    @Override
    public Unit visitExitStmt(Stmt.Exit stmt) {
        var value = visit(stmt.expr());

        throw new Redirect.Exit(value);
    }

    @Override
    public Unit visitFunStmt(Stmt.Fun stmt) {
        var copy = stmt.copy();

        if (copy.isLinked()) {
            copy.setLink(linker.getFunction(copy.path()).orElseThrow(() -> OahuError.missingFunctionLink(copy.path())));
        }

        copy.setScope(memory.peek());

        if (!memory.newLet(false, stmt.name().value(), copy)) {
            throw OahuError.redefinedName(stmt.name(), source, stmt.context());
        }

        return Unit.get();
    }

    @Override
    public Unit visitClassStmt(Stmt.Class stmt) {
        if (!memory.newLet(false, stmt.name().value(), stmt)) {
            throw OahuError.redefinedName(stmt.name(), source, stmt.context());
        }

        return Unit.get();
    }

    @Override
    public Unit visitEnumStmt(Stmt.Enum stmt) {
        return Unit.get();
    }

    public record Result(Object value, String repr) {
    }
}
