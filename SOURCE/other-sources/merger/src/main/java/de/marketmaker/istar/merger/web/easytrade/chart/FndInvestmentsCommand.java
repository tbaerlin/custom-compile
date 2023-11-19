/*
 * FndInvestmentsCommand.java
 *
 * Created on 29.08.2006 14:06:17
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.chart;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.common.validator.Range;
import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.merger.web.easytrade.ProviderSelectionCommand;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FndInvestmentsCommand extends BaseImgSymbolCommand implements
        ProviderSelectionCommand {

    private String providerPreference;

    private static final String DEFAULT_LABEL = "Unbekannt";

    public enum LabelType {
        FULL, NUMBER
    }

    @RestrictedSet("FWW,VWDIT,VWDBENL,MORNINGSTAR,SSAT,FIDA,VWD")
    public String getProviderPreference() {
        return providerPreference;
    }

    public void setProviderPreference(String providerPreference) {
        this.providerPreference = providerPreference;
    }

    private String allokationstyp;

    private String defaultLabel;

    private Integer numItems;

    private Double minimumValue;

    private boolean byValue = true;

    private boolean withConsolidatedAllocations = false;

    private LabelType labelType = LabelType.FULL;

    /**
     * Type of allocation to be returned
     */
    @NotNull
    @RestrictedSet("TYP,LAND,WAEHRUNG,UNTERNEHMEN,ASSET,COUNTRY,EXCHANGE,DURATION,SECTOR,CURRENCY,REAL_ESTATE,BONDS,RATING,INSTRUMENT,FUNDMANAGEMENT_STRATEGY," +
            "RISK_COUNTRY,ISSUER,STANDARDIZED_COUNTRY,FUND,ASSET_COUNTRY,STANDARDIZED_SECTOR,EXPOSURE,POOL,RISK,BASIC,THEME")
    public String getAllokationstyp() {
        return allokationstyp;
    }

    public void setAllokationstyp(String allokationstyp) {
        this.allokationstyp = allokationstyp;
    }

    public void setType(String type) {
        this.allokationstyp = type;
    }

    public void setWithConsolidatedAllocations(boolean withConsolidatedAllocations) {
        this.withConsolidatedAllocations = withConsolidatedAllocations;
    }

    public boolean isWithConsolidatedAllocations() {
        return withConsolidatedAllocations;
    }

    @Override
    public StringBuilder appendParameters(StringBuilder sb) {
        super.appendParameters(sb);
        sb.append("&allokationstyp=").append(this.allokationstyp)
                .append("&byValue=").append(this.byValue);
        if (this.defaultLabel != null) {
            sb.append("&defaultLabel=").append(this.defaultLabel);
        }
        if (this.numItems != null) {
            sb.append("&numItems=").append(this.numItems);
        }
        if (this.minimumValue != null) {
            sb.append("&minimumValue=").append(this.minimumValue);
        }
        if (this.withConsolidatedAllocations) {
            sb.append("&withConsolidatedAllocations=true");
        }
        if (this.providerPreference != null) {
            sb.append("&providerPreference=").append(this.providerPreference);
        }
        return sb;
    }

    @Range(min = 10, max = 1000)
    public int getHeight() {
        return super.getHeight();
    }

    @Range(min = 10, max = 1000)
    public int getWidth() {
        return super.getWidth();
    }

    @MmInternal
    public boolean isByValue() {
        return byValue;
    }

    public void setByValue(boolean byValue) {
        this.byValue = byValue;
    }

    /**
     * Label type "FULL" renders labels as <em>n% name</em>, whereas "NUMBER" renders labels
     * from 1 to <en>n</en>, which is convenient if percentages and names will be displayed
     * in a separate table.
     */
    public LabelType getLabelType() {
        return labelType;
    }

    public void setLabelType(LabelType labelType) {
        this.labelType = labelType;
    }

    /**
     * Maximum number of items to be added to the chart, additional items will be ignored
     */
    public Integer getNumItems() {
        return numItems;
    }

    public void setNumItems(Integer numItems) {
        this.numItems = numItems;
    }

    /**
     * Allocation items with a share less than this will not be added to the chart.
     * <b>Ignored</b> if <tt>numItems</tt> is defined.
     */
    public Double getMinimumValue() {
        return minimumValue;
    }

    public void setMinimumValue(Double minimumValue) {
        this.minimumValue = minimumValue;
    }

    /**
     * If the provided allocations do not add up to 100%, a new item will be added with the
     * remaining share and that item's label will be this value, default is "{@value #DEFAULT_LABEL}".
     */
    public String getDefaultLabel() {
        return this.defaultLabel == null ? DEFAULT_LABEL : this.defaultLabel;
    }

    public void setDefaultLabel(String defaultLabel) {
        this.defaultLabel = defaultLabel;
    }
}
