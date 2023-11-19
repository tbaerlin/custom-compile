/*
 * EvaluatedPositionGrupparator.java
 *
 * Created on 9/15/14 4:00 PM
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author kmilyut
 */
public class EvaluatedPositionGrouparator {

    public static final GroupBy DEFAULT_GROUP_BY = GroupBy.MARKET;

    public static enum GroupBy {
        INSTRUMENT {
            @Override
            Object getGroupKey(EvaluatedPosition position) {
                return position.getQuote().getInstrument().getId();
            }
        }, MARKET {
            @Override
            Object getGroupKey(EvaluatedPosition position) {
                return position.getQuote().getInstrument().getId() + "::" + position.getQuote().getId();
            }
        };

        abstract Object getGroupKey(EvaluatedPosition position);
    }

    public static GroupBy getGroupBy(String groupBy) {
        try {
            return GroupBy.valueOf(groupBy.trim().toUpperCase());
        } catch (Exception e) {
            return DEFAULT_GROUP_BY;
        }
    }

    public static List<EvaluatedPosition> createGroupedPositions(List<EvaluatedPosition> positions,
            GroupBy groupBy, Map<String, PortfolioPositionNote> notes) {
        Map<Object, List<EvaluatedPosition>> groupedPositions = groupPositionsBy(positions, groupBy);

        List<EvaluatedPosition> result = new ArrayList<>();
        for (List<EvaluatedPosition> group : groupedPositions.values()) {
            GroupedEvaluatedPosition groupedPosition = GroupedEvaluatedPosition.createGroupedEvaluatedPosition(group, groupBy, notes);
            result.add(groupedPosition);
        }

        return result;
    }

    private static Map<Object, List<EvaluatedPosition>> groupPositionsBy(
            List<EvaluatedPosition> positions, GroupBy groupBy) {
        Map<Object, List<EvaluatedPosition>> groupedPositions = new HashMap<>();

        for (EvaluatedPosition position : positions) {
            Object groupKey = groupBy.getGroupKey(position);

            if (!groupedPositions.containsKey(groupKey)) {
                groupedPositions.put(groupKey, new ArrayList<EvaluatedPosition>());
            }

            groupedPositions.get(groupKey).add(position);
        }

        return groupedPositions;
    }
}
