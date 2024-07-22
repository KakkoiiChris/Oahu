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

import java.util.*;

public class OptionalMap<K, V> implements Map<K, V> {
    private final Map<K, V> map = new HashMap<>();
    
    @Override
    public int size() {
        return map.size();
    }
    
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }
    
    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }
    
    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }
    
    @Override
    public V get(Object key) {
        return map.get(key);
    }
    
    @Override
    public V put(K key, V value) {
        return map.put(key, value);
    }
    
    public Optional<V> tryGet(K key) {
        return Optional.ofNullable(get(key));
    }
    
    @Override
    public V remove(Object key) {
        return map.remove(key);
    }
    
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        map.putAll(m);
    }
    
    @Override
    public void clear() {
        map.clear();
    }
    
    @Override
    public Set<K> keySet() {
        return map.keySet();
    }
    
    @Override
    public Collection<V> values() {
        return map.values();
    }
    
    @Override
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }
}
