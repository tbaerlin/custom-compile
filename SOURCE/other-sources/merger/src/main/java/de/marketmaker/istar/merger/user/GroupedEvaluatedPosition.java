/*
 * GroupedEvaluatedPosition.java
 *
 * Created on 9/15/14 12:41 PM
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;

import static de.marketmaker.istar.merger.Constants.MC;

/**
 * @author kmilyut
 */
public abstract class GroupedEvaluatedPosition extends AbstractEvaluatedPosition {

    protected static EasytradeInstrumentProvider instrumentProvider;

    protected static IntradayProvider intradayProvider;

    protected List<EvaluatedPosition> positions;

    protected Map<String, PortfolioPositionNote> notes;

    public GroupedEvaluatedPosition(List<EvaluatedPosition> positions,
            Map<String, PortfolioPositionNote> notes) {
        this.positions = positions;
        this.notes = notes;
    }

    @Override
    public BigDecimal getRealizedCost() {
        BigDecimal realizedCost = new BigDecimal(0);
        for (EvaluatedPosition position : positions) {
            realizedCost = realizedCost.add(position.getRealizedCost());
        }

        return realizedCost;
    }

    @Override
    public BigDecimal getRealizedGain() {
        BigDecimal realizedGain = BigDecimal.ZERO;
        for (EvaluatedPosition position : positions) {
            realizedGain = realizedGain.add(position.getRealizedGain());
        }

        return realizedGain;
    }

    @Override
    public BigDecimal getOrderValue() {
        BigDecimal orderValue = new BigDecimal(0);
        for (EvaluatedPosition position : positions) {
            orderValue = orderValue.add(position.getOrderValue());
        }

        return orderValue;
    }

    @Override
    public BigDecimal getOrderValueInPortfolioCurrency() {
        BigDecimal orderValue = new BigDecimal(0);
        for (EvaluatedPosition position : positions) {
            orderValue = orderValue.add(position.getOrderValueInPortfolioCurrency());
        }

        return orderValue;
    }

    @Override
    public BigDecimal getCurrentValue() {
        BigDecimal currentValue = new BigDecimal(0);
        for (EvaluatedPosition position : positions) {
            currentValue = currentValue.add(position.getCurrentValue());
        }

        return currentValue;
    }

    @Override
    public BigDecimal getCurrentValueInPortfolioCurrency() {
        BigDecimal currentValue = new BigDecimal(0);
        for (EvaluatedPosition position : positions) {
            currentValue = currentValue.add(position.getCurrentValueInPortfolioCurrency());
        }

        return currentValue;
    }

    @Override
    public BigDecimal getPreviousCloseValue() {
        BigDecimal closeValue = new BigDecimal(0);
        for (EvaluatedPosition position : positions) {
            closeValue = closeValue.add(position.getPreviousCloseValue());
        }

        return closeValue;
    }

    @Override
    public BigDecimal getPreviousCloseValueInPortfolioCurrency() {
        BigDecimal closeValue = new BigDecimal(0);
        for (EvaluatedPosition position : positions) {
            closeValue = closeValue.add(position.getPreviousCloseValueInPortfolioCurrency());
        }

        return closeValue;
    }

    @Override
    public BigDecimal getAverageOrderPrice() {
        BigDecimal totalVolume = getTotalVolume();
        if (totalVolume.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return getOrderValue().divide(totalVolume, MC).divide(getQuotedPerFactor(), MC);
    }

    @Override
    public BigDecimal getAverageOrderPriceInPortfolioCurrency() {
        return getOrderValueInPortfolioCurrency().divide(getQuotedPerFactor(), MC)
                .divide(getTotalVolume(), 2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal getTotalVolume() {
        BigDecimal totalVolume = new BigDecimal(0);
        for (EvaluatedPosition position : positions) {
            totalVolume = totalVolume.add(position.getTotalVolume());
        }

        return totalVolume;
    }

    @Override
    public PriceRecord getCurrentPrice() {
        return intradayProvider.getPriceRecords(Arrays.asList(getQuote())).get(0);
    }

    @Override
    public BigDecimal getExchangerate() {
        return null;
    }

    @Override
    public DateTime getLastOrderDate() {
        List<DateTime> dates = new ArrayList<>();

        for (EvaluatedPosition position : positions) {
            dates.add(position.getLastOrderDate());
        }

        dates.sort(null);

        return dates.get(dates.size() - 1);
    }

    public static void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        GroupedEvaluatedPosition.instrumentProvider = instrumentProvider;
    }

    public static void setIntradayProvider(IntradayProvider intradayProvider) {
        GroupedEvaluatedPosition.intradayProvider = intradayProvider;
    }

    public static GroupedEvaluatedPosition createGroupedEvaluatedPosition(
            List<EvaluatedPosition> positions, EvaluatedPositionGrouparator.GroupBy groupBy,
            Map<String, PortfolioPositionNote> notes) {
        switch (groupBy) {
            case MARKET:
                return new GroupedByMarketEvaluatedPosition(positions, notes);
            case INSTRUMENT:
                return new GroupedByInstrumentEvaluatedPosition(positions, notes);
        }

        return null;
    }

    @Override
    public PortfolioPosition getPosition() {
        return positions.get(0).getPosition();
    }

    public List<PortfolioPositionNote> getNotes() {
        if (notes == null) {
            return Collections.emptyList();
        }

        List<PortfolioPositionNote> result = new ArrayList<>();
        for (EvaluatedPosition position : positions) {
            String quoteId = position.getQuote().getId() + ".qid";

            if (notes.containsKey(quoteId)) {
                result.add(notes.get(quoteId));
            }
        }

        String instrumentId = positions.get(0).getQuote().getInstrument().getId() + ".iid";
        if (notes.containsKey(instrumentId)) {
            result.add(notes.get(instrumentId));
        }

        addMarketVwd(result);

        return result;
    }

    private void addMarketVwd(List<PortfolioPositionNote> notes) {
        for (PortfolioPositionNote note : notes) {
            String[] splitted = note.getItemId().split("\\.");

            if (splitted.length == 2 && splitted[1].equals("qid")) {
                Long qid = Long.parseLong(splitted[0]);
                note.setItemName(getMarketVwd(qid));
            }
        }
    }

    private String getMarketVwd(Long qid) {
        List<Quote> quotes = instrumentProvider.identifyQuotes(Collections.singleton(qid));
        Quote q = quotes.get(0);
        return q.getSymbolVwdfeedMarket();
    }

    public static class GroupedByMarketEvaluatedPosition extends GroupedEvaluatedPosition {

        GroupedByMarketEvaluatedPosition(List<EvaluatedPosition> positions,
                Map<String, PortfolioPositionNote> notes) {
            super(positions, notes);
        }

        @Override
        public Quote getQuote() {
            return positions.get(0).getQuote();
        }
    }

    public static class GroupedByInstrumentEvaluatedPosition extends GroupedEvaluatedPosition {

        GroupedByInstrumentEvaluatedPosition(List<EvaluatedPosition> positions,
                Map<String, PortfolioPositionNote> notes) {
            super(positions, notes);
        }

        @Override
        public Quote getQuote() {
            return instrumentProvider.getQuote(positions.get(0).getQuote().getInstrument(), null);
        }
    }
}

