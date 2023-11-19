/*
 * PartitionUtil.java
 *
 * Created on 12.03.2015 12:17
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author jkirchg
 */
public class PartitionUtil {

    private final static int MIN_PARTITION_SIZE = 20;

    public static List<Partition> partition(List<String> items) {
        return partition(items, MIN_PARTITION_SIZE);
    }

    public static List<Partition> partition(List<String> items, int minPartitionSize) {
        if (minPartitionSize < 0) {
            minPartitionSize = MIN_PARTITION_SIZE;
        }

        final List<Partition> result = new ArrayList<>();

        final Map<Character, List<String>> alphaMap =
                items.stream()
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .collect(Collectors.groupingBy(s -> Character.toUpperCase(s.charAt(0))));

        int offset = 0;
        List<Character> keys = new ArrayList<>(alphaMap.keySet());
        keys.sort(null);
        for (char key : keys) {
            List<String> alphaList = alphaMap.get(key);
            final Partition previousPartition = result.size() > 0 ? result.get(result.size() - 1) : null;
            final Partition partition = new Partition(offset, alphaList.get(0), alphaList.get(alphaList.size() - 1));

            if (previousPartition == null || (offset - previousPartition.getOffset() > minPartitionSize)) {
                // create a new partition
                result.add(partition);
            }
            else {
                // add to the previous partition
                previousPartition.merge(partition);
            }
            offset = offset + alphaList.size();
        }

        return result;
    }

}
