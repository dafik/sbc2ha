package com.dfi.sbc2ha.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DataUtil {
    public static <T> List<Set<T>> split(Set<T> originalSet, int count) {
        int length = (int) Math.ceil((double) originalSet.size() / count);
        List<Set<T>> result = new ArrayList<>(count);
        for (int i = 0; i < length; i++) {
            result.add(new LinkedHashSet<>());
        }
        int index = 0;
        for (T object : originalSet) {
            Set<T> rs = result.get(index++ / count);

            rs.add(object);
        }

        return result;
    }

    public static <T> List<List<T>> split(List<T> originalSet, int count) {
        int length = (int) Math.ceil((double) originalSet.size() / count);
        List<List<T>> result = new ArrayList<>(count);
        for (int i = 0; i < length; i++) {
            result.add(new ArrayList<>());
        }
        int index = 0;
        for (T object : originalSet) {
            List<T> rs = result.get(index++ / count);

            rs.add(object);
        }

        return result;
    }
}
