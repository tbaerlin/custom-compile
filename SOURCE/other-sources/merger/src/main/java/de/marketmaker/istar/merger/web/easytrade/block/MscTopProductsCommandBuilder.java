/*
 * MscTopProductsQueryExecutor.java
 *
 * Created on 06.06.12 10:09
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @author Michael Lösch
 */

public abstract class MscTopProductsCommandBuilder {

    public enum ProductType {
        BONUS("gapBarrierRelative",
                "interval:10",
                "yieldRelativePerYear",
                new String[]{"typeKey=='+CERT_BONUS'", "expirationDate=='6m_x'"},
                new String[]{"20000", "30000", "40000", "50000"},
                false),
        DISCOUNT("capLevel",
                "interval:20",
                "maximumYieldRelativePerYear",
                new String[]{"typeKey=='+CERT_DISCOUNT'", "expirationDate=='6m_x'"},
                new String[]{"60000", "80000", "100000", "120000"},
                false),
        DEEPDISCOUNT("capLevel",
                "interval:20",
                "maximumYieldRelativePerYear",
                new String[]{"typeKey=='+CERT_DISCOUNT'", "expirationDate=='12m_x'"},
                new String[]{"40000", "60000", "80000", "100000", "120000"},
                false),
        INDEX("expirationDate",
                "interval:x_12m;x_180m;NULL",
                "performance3m",
                new String[]{"typeKey=='+CERT_INDEX'"},
                new String[]{"x_12m", "x_180m", "NULL"},
                false),
        REVERSECONVERTIBLE("gapStrikeRelative",
                "interval:10",
                "performance3m",
                new String[]{"typeKey=='+CERT_REVERSE_CONVERTIBLE'"},
                new String[]{"-10000", "0", "10000", "20000", "30000"},
                false),
        TURBO("gapStrikeRelative",
                "interval:5",
                "leverage",
                new String[]{"subtype=='+Turbos'", "expirationDate == '6m_x'"},
                new String[]{"5000", "10000", "15000", "20000", "25000"},
                true),
        MINIFUTURES("gapStrikeRelative",
                "interval:5",
                "leverage",
                new String[]{"subtype=='Minifutures'", "expirationDate == '6m_x'"},
                new String[]{"5000", "10000", "15000", "20000", "25000", "30000", "35000"},
                true);

        private final String[] params;
        private final InstrumentTypeEnum instrumentType;
        private final boolean ascending;
        private final String primaryField;
        private final String secondaryField;
        private final String secondaryFieldOperator;
        private final String sortField;
        private final int numResults;
        private final String[] columns;
        private final boolean leverage;

        private ProductType(String secondaryField, String secondaryFieldOperator,
                            String sortField, String[] queryParams, String[] columns,
                            boolean leverage) {
            this.instrumentType = InstrumentTypeEnum.CER;
            this.primaryField = "underlyingIid";
            this.secondaryField = secondaryField;
            this.secondaryFieldOperator = secondaryFieldOperator;
            this.sortField = sortField;
            this.ascending = false;
            this.numResults = 1;
            this.params = queryParams;
            this.columns = columns;
            this.leverage = leverage;
        }

        public List<String> getColumns() {
            return Arrays.asList(this.columns);
        }

        public String[] getParams() {
            return params;
        }

        public String getSecondaryField() {
            return secondaryField;
        }

        public String getPrimaryField() {
            return primaryField;
        }

        public String getSortField() {
            return sortField;
        }

        public InstrumentTypeEnum getInstrumentType() {
            return instrumentType;
        }

        public boolean isLeverage() {
            return this.leverage;
        }

        public String getSecondaryFieldOperator() {
            return secondaryFieldOperator;
        }

        @Override
        public String toString() {
            // do not change, stringtemplate rendering depends on it
            return name();
        }
    }


    private static String buildQuery(ProductType type, String issuerName, List<Quote> quotes) {
        final String[] params = type.params;
        final StringBuilder sb = new StringBuilder();
        for (String param : params) {
            if (sb.length() != 0) {
                sb.append(" && ");
            }
            sb.append(param);
        }
        if (StringUtils.hasText(issuerName)) {
            sb.append(" && issuername=='").append(issuerName).append("'");
        }
        if (quotes != null && !quotes.isEmpty()) {
            sb.append(" && underlyingIid=='").append(getInstrumentsIids(quotes)).append("'");
        }
        return sb.toString();
    }

    private static String getInstrumentsIids(List<Quote> quotes) {
        final StringBuilder sb = new StringBuilder();
        for (Quote quote : quotes) {
            if (sb.length() != 0) {
                sb.append("@");
            }
            sb.append(quote.getInstrument().getId());
        }
        return sb.toString();
    }

    public static BestToolCommand buildCommand(MscTopProducts.TopCommand topCommand, List<Quote> quotes) {
        final BestToolCommand cmd = new BestToolCommand();
        final ProductType type = topCommand.getProductType();
        cmd.setQuery(buildQuery(type, topCommand.getIssuername(), quotes));
        cmd.setType(type.instrumentType);
        cmd.setPrimaryField(type.primaryField);
        cmd.setNumResults(type.numResults);
        cmd.setAscending(type.ascending);
        cmd.setSecondaryField(type.secondaryField);
        cmd.setSecondaryFieldOperator(type.secondaryFieldOperator);
        cmd.setDisablePaging(topCommand.isDisablePaging());
        cmd.setSortField(type.sortField);
        return cmd;
    }
}