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
package kakkoiichris.oahu.script.data;

public class Null extends Instance {
    private static Null instance;
    
    public static Null get() {
        if (instance == null) {
            instance = new Null();
        }
        
        return instance;
    }
    
    private Null() {
        super(null, null, null);
    }
    
    @Override
    public String toString() {
        return "null";
    }
}
