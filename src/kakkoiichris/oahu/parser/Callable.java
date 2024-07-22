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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public sealed interface Callable<T extends Callable.Param> permits Stmt.Fun, Stmt.Class {
    int arity();
    
    List<T> params();
    
    default Optional<List<Expr>> resolve(List<Expr.Invoke.Arg> args) {
        var params = params();
        
        var isVararg = !params.isEmpty() && params.getLast().isVarargs();
        
        if (args.size() > arity() && !isVararg) {
            return Optional.empty();
        }
        
        var exprs = Stream
            .generate(() -> (Expr) Expr.Empty.get())
            .limit(params.size())
            .collect(Collectors.toList());
        
        var split = args
            .stream()
            .collect(Collectors.groupingBy(Expr.Invoke.Arg::isPositional));
        
        var named = split.get(false);
        
        if (!named.isEmpty()) {
            for (var arg : named) {
                var index = IntStream
                    .range(0, params.size())
                    .filter(i -> params.get(i).name().equals(arg.name()))
                    .findFirst()
                    .orElse(-1);
                
                if (index < 0) {
                    return Optional.empty();
                }
                
                exprs.set(index, arg.expr());
            }
        }
        
        var positional = split.get(true);
        
        if (!positional.isEmpty()) {
            var p = 0;
            
            for (var i = 0; i < exprs.size(); i++) {
                if (exprs.get(i) == Expr.Empty.get()) {
                    if (p < positional.size()) {
                        if (!params.get(i).isVarargs()) {
                            exprs.set(i, positional.get(p++).expr());
                        }
                    }
                    else if (params.get(i).defaultValue() != Expr.Empty.get()) {
                        exprs.set(i, params.get(i).defaultValue());
                    }
                }
            }
            
            if (p < positional.size()) {
                var varargs = new ArrayList<Expr>();
                
                while (p < positional.size()) {
                    var arg = positional.get(p++);
                    
                    varargs.add(arg.spread() ? Expr.toExpr(new Expr.Invoke.Spread(arg.expr())) : arg.expr());
                }
                
                exprs.set(exprs.size() - 1, Expr.toExpr(new Expr.Invoke.Vararg(varargs)));
            }
        }
        
        for (var i = 0; i < params.size(); i++) {
            var param = params.get(i);
            
            if (exprs.get(i) == Expr.Empty.get()) {
                exprs.set(i, param.defaultValue());
            }
        }
        
        if (isVararg && exprs.getLast() == Expr.Empty.get()) {
            exprs.set(exprs.size() - 1, Expr.toExpr(new Expr.Invoke.Vararg(List.of())));
        }
        
        if (exprs.stream().allMatch(expr -> expr != Expr.Empty.get())) {
            return Optional.of(exprs);
        }
        
        return Optional.empty();
    }
    
    interface Param {
        boolean isMutable();

        boolean isVarargs();
        
        Expr.Name name();
        
        Expr defaultValue();
    }
}
