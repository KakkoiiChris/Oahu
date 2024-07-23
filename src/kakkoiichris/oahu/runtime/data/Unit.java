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
package kakkoiichris.oahu.runtime.data;

public class Unit {
    private static Unit instance;
    
    public static Unit get() {
        if (instance == null) {
            instance = new Unit();
        }
        
        return instance;
    }
    
    private Unit() {
    }
    
    @Override
    public String toString() {
        return "unit";
    }
}
