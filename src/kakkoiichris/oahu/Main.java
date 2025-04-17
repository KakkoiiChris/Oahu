/***************************************************
 *    ____  _       _    _ _    _                  *
 *   / __ \ \|/\   | |  | | |  | |      /\         *
 *  | |  | | /  \  | |__| | |  | | ____/  \_       *
 *  | |  | |/ /\ \ |  __  | |  | |\         |      *
 *  | |__| / ____ \| |  | | |__| | \         \/|   *
 *   \____/_/    \_\_|  |_|\____/   \___/\__   \   *
 *                                          \___\  *
 *        Copyright (C) 2019, KakkoiiChris         *
 ***************************************************/

import kakkoiichris.kotoba.Console;
import kakkoiichris.kotoba.Font;
import kakkoiichris.oahu.runtime.Memory;
import kakkoiichris.oahu.util.OahuError;
import kakkoiichris.oahu.util.Source;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.Objects;

import static kakkoiichris.oahu.util.Aesthetics.ICON;

void main(String[] args) throws InterruptedException, IOException {
    var config = new Console.Config()
        .title("O'ahu Console")
        .font(new Font("/font/Consolas24.bff"))
        .icon(ImageIO.read(Objects.requireNonNull(Source.class.getResourceAsStream("/icon.png"))));

    var console = new Console(config);

    console.open();

    switch (args.length) {
        case 0 -> repl(console);

        case 1 -> file(console, args[0]);

        default -> console.writeLine("Usage: oahu [fileName]");
    }

    console.pause();

    console.close();
}

@SuppressWarnings({"preview", "BusyWait"})
private void repl(Console console) throws InterruptedException {
    console.writeLine("""
          ____  _       _    _ _    _
         / __ \\ \\|/\\   | |  | | |  | |      /\\
        | |  | | /  \\  | |__| | |  | | ____/  \\_
        | |  | |/ /\\ \\ |  __  | |  | |\\         |
        | |__| / ____ \\| |  | | |__| | \\         \\/|
         \\____/_/    \\_\\_|  |_|\\____/   \\___/\\__   \\
                                                \\___\\
              Copyright (C) 2019, KakkoiiChris
        """);

    console.setPrompt("O'ahu > ");

    do {
        var code = console.readLine();

        if (code.isEmpty()) {
            break;
        }

        try {
            var source = Source.ofREPL(code.get());

            var script = source.prepare();

            var result = script.run();

            console.writeLine(Memory.fromReference(result.value()));
        }
        catch (OahuError error) {
            console.setColor(0xFF0000);
            console.writeLine(error.getMessage());
            console.setColor(0xFFFFFF);

            Thread.sleep(20);
        }
    }
    while (console.isOpen());
}

private void file(Console console, String path) {
    try {
        var source = Source.ofFile(path);

        var script = source.prepare();

        var result = script.run();

        console.writeLine(Memory.fromReference(result.value()));
    }
    catch (OahuError error) {
        console.setColor(0xFF0000);
        console.writeLine(error.getMessage());
        console.setColor(0xFFFFFF);
    }
}