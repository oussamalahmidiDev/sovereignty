package com.oussama.sovereignty.application.common;

import java.util.ArrayList;
import java.util.List;

public final class ListUtils {

    private ListUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static <T> List<List<T>> partition(List<T> list, int size) {
        if (list == null) {
            throw new IllegalArgumentException("List must not be null");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be greater than 0");
        }
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return partitions;
    }
}
