package kakkoiichris.oahu.runtime.linker;

import kakkoiichris.oahu.lexer.Context;
import kakkoiichris.oahu.parser.Expr;
import kakkoiichris.oahu.parser.Stmt;
import kakkoiichris.oahu.util.OahuError;
import kakkoiichris.oahu.util.OahuWarning;
import kakkoiichris.oahu.util.Source;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Linker {
    private static final Map<String, Link> standardLinks;

    private final Source source;
    private final Map<String, Link> externalLinks;

    private final List<Link> usedLinks = new ArrayList<>();

    private final Map<String, Stmt.Fun.Link> functions = new HashMap<>();
    private final Map<String, Stmt.Class.Link> classes = new HashMap<>();

    private boolean warn = true;

    static {
         standardLinks = associateLinks(
             CoreLink.get()/*,
             MathLink.get(),
             FileLink.get(),
             GFXLink.get()*/
         );
    }

    public Linker(Source source, Link... links) {
        this.source = source;

        externalLinks = associateLinks(links);
    }

    public static Map<String, Link> associateLinks(Link... links) {
        return Arrays
            .stream(links)
            .collect(Collectors.toMap(Link::getName, Function.identity()));
    }

    public Optional<Source> importLink(String name) {
        return importLink(new Expr.Name(Context.none(), name));
    }

    public Optional<Source> importLink(Expr.Name name) {
        var link = standardLinks.get(name.value());

        if (link == null) {
            link = externalLinks.get(name.value());
        }

        if (link == null) {
            throw OahuError.missingLink(name);
        }

        if (usedLinks.contains(link)) {
            OahuWarning.duplicateLink(name,source);

            return Optional.empty();
        }

        link.addFunctions((String path, Stmt.Fun.Link funLink) -> {
            if (!functions.containsKey(path)) {
                functions.put(path, funLink);
            }
        });

        link.addClasses((String path, Stmt.Class.Link classLink) -> {
            if (!classes.containsKey(path)) {
                classes.put(path, classLink);
            }
        });

        usedLinks.add(link);

        return Optional.of(link.getSource());
    }

    public Optional<Stmt.Fun.Link> getFunction(String path) {
        if (!functions.containsKey(path)){
            return Optional.empty();
        }

        return Optional.of(functions.get(path));
    }

    public Optional<Stmt.Class.Link> getClass(String path) {
        if (!classes.containsKey(path)){
            return Optional.empty();
        }

        return Optional.of(classes.get(path));
    }

    public void close() {
        for (var link : usedLinks) {
            link.close();
        }

        usedLinks.clear();

        functions.clear();
        classes.clear();
    }
}