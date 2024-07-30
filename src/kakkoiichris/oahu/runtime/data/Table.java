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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

    public boolean add(Object element) {
        return list.add(element);
    }

    public void add(int index, Object element) {
        list.add(index, element);
    }

    public boolean addAll(List<Object> elements) {
        return list.addAll(elements);
    }

    public Object remove(int index) {
        return list.remove(index);
    }

    public void remove(Object element) {
        list.remove(element);
    }

    public Object get(int index) {
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

    public Table repeat(int count) {
        var result = Table.empty();

        for (var i = 0; i < count; i++) {
            result.addAll(list);
        }

        return result;
    }

    public void unshift(Object element) {
        list.addFirst(element);
    }

    public Object shift() {
        return list.removeFirst();
    }

    public void push(Object element) {
        list.addLast(element);
    }

    public Object pop() {
        return list.removeLast();
    }

    public Object random() {
        return get((int) (Math.random() * size()));
    }

    public boolean contains(Object element) {
        return list.contains(element);
    }
}
