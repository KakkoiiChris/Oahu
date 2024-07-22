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
package kakkoiichris.oahu.util;

import kakkoiichris.oahu.lexer.Lexer;
import kakkoiichris.oahu.parser.Parser;
import kakkoiichris.oahu.script.Script;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public record Source(String name, String text) {
    public static Source ofResource(String resourcePath) {
        Path path;

        try {
            path = Path.of(ClassLoader.getSystemResource(resourcePath).toURI());
        }
        catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        var name = path.getFileName().toString();

        String text;

        try {
            text = Files.readString(path);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new Source(name, text);
    }

    public static Source ofFile(String filePath) {
        var path = Path.of(filePath);

        var name = path.getFileName().toString();

        String text;

        try {
            text = Files.readString(path);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new Source(name, text);
    }

    public static Source ofREPL(String text) {
        return new Source("<REPL>", text);
    }

    public String getLine(int row) {
        return text.lines().toList().get(row - 1);
    }

    public Script prepare() {
        var lexer = new Lexer(this);

        var parser = new Parser(this, lexer);

        var program = parser.parse();

        return new Script(this, program);
    }
}
