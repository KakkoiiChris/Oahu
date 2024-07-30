package kakkoiichris.oahu.runtime.linker;

import kakkoiichris.oahu.parser.Stmt;
import kakkoiichris.oahu.util.Source;

import java.util.function.BiConsumer;

public interface Link {
    String getName();

    Source getSource();

    void addFunctions(BiConsumer<String, Stmt.Fun.Link> addFunction);

    void addClasses(BiConsumer<String, Stmt.Class.Link> addClass);

    void close();
}