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
import kakkoiichris.oahu.util.OahuError;
import kakkoiichris.oahu.util.Source;

import static kakkoiichris.oahu.util.Aesthetics.PALM;

void main() {
    System.out.println("""
          ____  _       _    _ _    _
         / __ \\ \\|/\\   | |  | | |  | |      /\\
        | |  | | /  \\  | |__| | |  | | ____/  \\_
        | |  | |/ /\\ \\ |  __  | |  | |\\         |
        | |__| / ____ \\| |  | | |__| | \\         \\/|
         \\____/_/    \\_\\_|  |_|\\____/   \\___/\\__   \\
                                                \\___\\
              Copyright (C) 2019, KakkoiiChris""");
    
    while (true) {
        var code = System.console()
            .readLine(STR."\{PALM} ");
        
        if (code.isBlank()) {
            break;
        }
        
        try {
            var source = Source.ofREPL(code);
            
            var script = source.prepare();
            
            var result = script.run();
            
            System.out.println(result.value());
        }
        catch (OahuError error) {
            error.printStackTrace();
        }
    }
}