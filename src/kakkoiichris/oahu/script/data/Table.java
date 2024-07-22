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

import java.util.*;

public record Table(List<Object> list, Map<String, Object> map) implements Iterable<Object> {
    public static Table empty() {
        return new Table(List.of(), Map.of());
    }
    
    @Override
    public Iterator<Object> iterator() {
        return list.iterator();
    }
    
    public int size() {
        return list.size();
    }
    
    public Object get(int index){
        return list.get(index);
    }
    
    public void set(int index, Object element) {
        list.set(index, element);
    }
    
    public Object get(String key) {
        return map.get(key);
    }
    
    public void set(String key, Object value) {
        map.put(key, value);
    }
}
