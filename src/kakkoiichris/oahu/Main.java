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

import kakkoiichris.oahu.runtime.Memory;
import kakkoiichris.oahu.util.OahuError;
import kakkoiichris.oahu.util.Source;

import java.util.Scanner;

import static kakkoiichris.oahu.util.Aesthetics.ICON;

void main(String... args) throws InterruptedException {
    switch (args.length) {
        case 0 -> repl();

        case 1 -> file(args[0]);
    }
}

@SuppressWarnings({"preview", "BusyWait"})
private void repl() throws InterruptedException {
    System.out.println("""
          ____  _       _    _ _    _
         / __ \\ \\|/\\   | |  | | |  | |      /\\
        | |  | | /  \\  | |__| | |  | | ____/  \\_
        | |  | |/ /\\ \\ |  __  | |  | |\\         |
        | |__| / ____ \\| |  | | |__| | \\         \\/|
         \\____/_/    \\_\\_|  |_|\\____/   \\___/\\__   \\
                                                \\___\\
              Copyright (C) 2019, KakkoiiChris
        \s""");

    try (var in = new Scanner(System.in)) {
        while (in.hasNextLine()) {
            System.out.print(STR."O'ahu \{ICON} ");

            var code = in.nextLine();

            if (code.isBlank()) {
                break;
            }

            try {
                var source = Source.ofREPL(code);

                var script = source.prepare();

                var result = script.run();

                System.out.println(Memory.fromReference(result.value()));
            }
            catch (OahuError error) {
                System.err.println(error.getMessage());

                Thread.sleep(20);
            }
        }
    }
}

private void file(String path) {
    try {
        var source = Source.ofFile(path);

        var script = source.prepare();

        var result = script.run();

        System.out.println(Memory.fromReference(result.value()));
    }
    catch (OahuError error) {
        System.err.println(error.getMessage());
    }
}