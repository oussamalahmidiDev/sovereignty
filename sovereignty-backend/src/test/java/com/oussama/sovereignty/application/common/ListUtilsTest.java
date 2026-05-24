package com.oussama.sovereignty.application.common;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ListUtilsTest {

    @Test
    void partition_splitsListCorrectly() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);
        List<List<Integer>> result = ListUtils.partition(list, 3);

        assertEquals(3, result.size());
        assertEquals(Arrays.asList(1, 2, 3), result.get(0));
        assertEquals(Arrays.asList(4, 5, 6), result.get(1));
        assertEquals(Arrays.asList(7, 8), result.get(2));
    }

    @Test
    void partition_handlesExactMultiple() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4);
        List<List<Integer>> result = ListUtils.partition(list, 2);

        assertEquals(2, result.size());
        assertEquals(Arrays.asList(1, 2), result.get(0));
        assertEquals(Arrays.asList(3, 4), result.get(1));
    }

    @Test
    void partition_throwsOnNullList() {
        assertThrows(IllegalArgumentException.class, () -> ListUtils.partition(null, 2));
    }

    @Test
    void partition_throwsOnInvalidSize() {
        List<Integer> list = Arrays.asList(1, 2);
        assertThrows(IllegalArgumentException.class, () -> ListUtils.partition(list, 0));
        assertThrows(IllegalArgumentException.class, () -> ListUtils.partition(list, -1));
    }
}
