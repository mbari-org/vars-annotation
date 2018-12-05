package org.mbari.m3.vars.annotation.util;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-10-18T14:35:00
 */
public class ListUtils {

    /**
     * Find an item the contains a key in a list
     * @param key The string to search for
     * @param items A list of objects
     * @param startIdx The point in the list to start the search
     * @param fn A function that converts
     * @param <T>
     * @return
     */
    public static <T> Optional<T> search(String key,
                                         List<T> items,
                                         int startIdx,
                                         Function<T, String> fn) {
        if (startIdx >= items.size()) {
            startIdx = 0;
        }
        List<T> searchableItems = items;
        // Shuffle list from such that: items[startId:end] :: items[0, startIdx - 1]
        if (startIdx > 0) {
            searchableItems = new ArrayList<>(items.subList(startIdx, items.size()));
            searchableItems.addAll(items.subList(0, startIdx));
        }
        return searchableItems.stream()
                .filter(t -> fn.apply(t).contains(key))
                .findFirst();
    }

    public static <T> List<T> union(List<T> list1, List<T> list2) {
        Set<T> set = new HashSet<>();

        set.addAll(list1);
        set.addAll(list2);

        return new ArrayList<>(set);
    }

    public static <T> List<T> intersection(Collection<T> list1, Collection<T> list2) {
//        return list1.stream()
//                .distinct()
//                .filter(list2::contains)
//                .collect(Collectors.toList());

        List<T> list = new ArrayList<>();

        for (T t : list1) {
            if(list2.contains(t)) {
                list.add(t);
            }
        }

        return list;
    }



}
