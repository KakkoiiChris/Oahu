package kakkoiichris.oahu.util;

public enum Stage {
    LEXER,
    PARSER,
    RUNTIME,
    LINKER;

    private final String REP = Util.toTitleCase(name());

    @Override
    public String toString() {
        return REP;
    }
}
